package com.ncc.savior.desktop.clipboard.windows;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.EmptyClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnicodeClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnknownClipboardData;
import com.ncc.savior.util.JavaUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Wrapper class wraps the windows clipboard into a generic
 * {@link IClipboardWrapper}.
 *
 * In order to get the Delayed Render to work with JNA, it seems you need the
 * following steps.
 * <ol>
 * <li>Create a window where we can edit the Window Proc and handle
 * WM_RENDERFORMAT and WM_RENDERALLFORMATS
 * <li>Write null to the clipboard using the entire process (Open, Empty, write,
 * close). Make sure the window handle is passed to the open function.
 * <li>Have a loop to clear the message queue via GetMessage or PeekMessage.
 * <ol>
 *
 *
 */
public class WindowsClipboardWrapper implements IClipboardWrapper {
	private static final Logger logger = LoggerFactory.getLogger(WindowsClipboardWrapper.class);
	// message for window creation. window is needed for the windows callback.
	protected static final int WM_NCCREATE = 0x0081;
	// Sent to clipboard owner when empty clipboard is called
	protected static final int WM_DESTROYCLIPBOARD = 0x0307;
	// Sent to clipboard viewers when clipboard changes
	protected static final int WM_DRAWCLIPBOARD = 0x0308;
	// Sent to clipboard owner who set clipboard to delayed render mode and someone
	// has tried to paste. Need to put data on clipboard.
	protected static final int WM_RENDERFORMAT = 0x0305;
	// Sent to clipboard owner before owner is destroyed so it can render all
	// formats. We probably want to ignore this. TODO figure out if ignore is
	// appropriate.
	protected static final int WM_RENDERALLFORMATS = 0x0306;

	static IWindowsClipboardUser32 user32 = IWindowsClipboardUser32.INSTANCE;
	static Kernel32 kernel32 = Kernel32.INSTANCE;

	WindowProc callback = new WindowProc() {
		@Override
		public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
			// logger.debug("got message(callback)=" + uMsg);
			// For the below, 1 is success where 0 represents failure
			switch (uMsg) {
			case WM_NCCREATE:
				return new LRESULT(1);
			case WM_DESTROYCLIPBOARD:
				onClipboardEmptied();
				return new LRESULT(1);
			case WM_DRAWCLIPBOARD:
				onClipboardChanged();
				return new LRESULT(1);
			case WM_RENDERFORMAT:
				onPaste(wParam);
				return new LRESULT(1);
			case WM_RENDERALLFORMATS:
				return new LRESULT(1);
			case User32.WM_DEVICECHANGE:
				return new LRESULT(1);

			default:
				return new LRESULT(0);
			}
		}
	};
	private HWND windowHandle;
	private ScheduledExecutorService executor;
	private IClipboardListener clipboardListener;

	public WindowsClipboardWrapper() {

		ThreadFactory threadFactory = new ThreadFactory() {
			private int i = 1;

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, getName());
			}

			private synchronized String getName() {
				// increments after returning value;
				return "clipboard-" + i++;
			}
		};
		// We need at least 2 threads. One will read messages forever and any others
		// will handle the other short running asynchronous tasks.
		this.executor = Executors.newScheduledThreadPool(3, threadFactory);

		executor.execute(new Runnable() {

			@Override
			public void run() {
				WString className = new WString("delayedRenderClipboardManager");
				String title = "title";
				WNDCLASSEX wx = new WNDCLASSEX();
				wx.clear();
				wx.lpszClassName = className;
				wx.lpfnWndProc = callback;

				while (User32.INSTANCE.RegisterClassEx(wx).intValue() == 0) {
					WindowsError error = getLastError();
					logger.error("Error registering class to window: " + error);
				}
				windowHandle = user32.CreateWindowEx(0, className, title, 0, 0, 0, 0, 0, null, null, null, null);
				user32.SetClipboardViewer(windowHandle);

				while (true) {
					// get all messages forever
					getMessage();
					JavaUtil.sleepAndLogInterruption(10);
				}
			}
		});
	}

	/**
	 * Sets the windows clipboard into delayed rendering mode for the given formats.
	 * In delayed rendering mode, the first time for each format where an
	 * application attempts to paste, it will callback and ask for the data. We will
	 * handle that callback.
	 */
	@Override
	public void setDelayedRenderFormats(Collection<ClipboardFormat> formats) {
		executor.schedule(new Runnable() {

			@Override
			public void run() {
				logger.debug("writing null to clipboard formats=" + formats);
				writeNullToClipboard(formats);
			}

		}, 1, TimeUnit.MICROSECONDS);

	}

	/**
	 * sets the listener for clipboard events.
	 */

	@Override
	public void setClipboardListener(IClipboardListener listener) {
		this.clipboardListener = listener;
	}

	/**
	 * When data is received from a delayed rendering call, this method is called to
	 * actually set that data.
	 */
	@Override
	public void setDelayedRenderData(ClipboardData clipboardData) {
		user32.SetClipboardData(clipboardData.getFormat().getWindows(), clipboardData.getWindowsData());
	}

	/**
	 * Called when delayed rendering callback is called due to a local application
	 * attempting to paste data.
	 *
	 * @param wParam
	 */
	protected void onPaste(WPARAM wParam) {
		if (clipboardListener != null) {
			clipboardListener.onPasteAttempt(ClipboardFormat.fromWindows(wParam.intValue()));
		} else {
			Pointer p = new Memory(1);
			p.setByte(0, (byte) 0);
			user32.SetClipboardData(wParam.intValue(), p);
		}
	}

	/**
	 * Called when a local application changes the clipboard.
	 */
	protected void onClipboardChanged() {
		HWND owner = user32.GetClipboardOwner();
		if (!windowHandle.equals(owner)) {
			executor.schedule(new Runnable() {
				@Override
				public void run() {
					Set<ClipboardFormat> formats = getClipboardFormatsAvailable();
					clipboardListener.onClipboardChanged(formats);
				}
			}, 1, TimeUnit.MICROSECONDS);
		}
	}

	/**
	 * Called when someone (including yourself) calls to empty your clipboard. This
	 * is likely to occur just before they write the clipboard. We don't use this at
	 * the moment because we are watching for the clipboard changed call.
	 */
	protected void onClipboardEmptied() {

	}

	/**
	 * Used to write null to the clipboard for all given formats. This sets the
	 * clipboard into delayed rendering mode for those formats.
	 *
	 * @param windowHandle2
	 * @param formats
	 */
	protected void writeNullToClipboard(Collection<ClipboardFormat> formats) {
		openClipboardWhenFree();
		try {
			boolean success = user32.EmptyClipboard();
			if (!success) {
				throw windowsErrorToException("Error emptying clipboard");
			}
			for (ClipboardFormat format : formats) {
				user32.SetClipboardData(format.getWindows(), Pointer.NULL);
				WindowsError error = getLastError();
				if (error.error != 0) {
					throw windowsErrorToException("Error writing NULL to clipboard with format=" + format, error);
				}
			}
		} catch (Throwable t) {
			throw windowsErrorToException("Error attempting to write null to clipboard for formats=" + formats, null,
					t);
		} finally {
			HWND owner = user32.GetClipboardOwner();
			if (windowHandle.equals(owner)) {
				user32.CloseClipboard();
			}
		}
	}

	private Set<ClipboardFormat> getClipboardFormatsAvailable() {
		IntByReference returnedSizeOfFormats = new IntByReference();
		int sizeOfFormats = 20;
		int[] formats = new int[sizeOfFormats];
		boolean success = user32.GetUpdatedClipboardFormats(formats, sizeOfFormats, returnedSizeOfFormats);
		if (success) {
			Set<ClipboardFormat> set = new HashSet<ClipboardFormat>();
			int[] arr = Arrays.copyOf(formats, returnedSizeOfFormats.getValue());
			for (int a : arr) {
				ClipboardFormat format = ClipboardFormat.fromWindows(a);
				if (format != null) {
					set.add(format);
				}
			}
			return set;
		} else {
			throw windowsErrorToException("Error getting clipboard formats.");
		}

	}

	private void getMessage() {
		MSG msg = new WinUser.MSG();
		boolean hasMessage = user32.PeekMessage(msg, windowHandle, 0, 0, 0);
		if (hasMessage) {
			user32.GetMessage(msg, windowHandle, 0, 0);
			// logger.debug("got message=" + msg.message);
			if (msg.message != 0xC228) {
			}
			user32.TranslateMessage(msg);
			user32.DispatchMessage(msg);
		}
	}

	private RuntimeException windowsErrorToException(String string, WindowsError error, Throwable t) {
		if (error == null) {
			error = getLastError();
		}
		String message = string + " Error=" + error + " WindowsMessage=" + error.errorMessage;
		if (t != null) {
			return new RuntimeException(message, t);
		} else {
			return new RuntimeException(message);
		}
	}

	private RuntimeException windowsErrorToException(String string) {
		return windowsErrorToException(string, null, null);
	}

	private RuntimeException windowsErrorToException(String string, WindowsError error) {
		return windowsErrorToException(string, error, null);
	}

	private static WindowsError getLastError() {
		int error = kernel32.GetLastError();
		int langId = 0;
		PointerByReference lpBuffer = new PointerByReference();
		kernel32.FormatMessage(WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER | WinBase.FORMAT_MESSAGE_FROM_SYSTEM
				| WinBase.FORMAT_MESSAGE_IGNORE_INSERTS, null, error, langId, lpBuffer, 0, null);
		return new WindowsError(error, lpBuffer.getValue().getWideString(0));
	}

	/**
	 * blocks/polls until success;
	 *
	 * @param windowHandle
	 * @return
	 */
	private void openClipboardWhenFree() {
		// logger.debug("attempting to open clipboard");
		boolean success = user32.OpenClipboard(windowHandle);
		// wait for clipboard to be free
		while (!success) {
			JavaUtil.sleepAndLogInterruption(1);
			logger.debug("clipboard unable to be opened.  Trying again.  Owner=" + user32.GetClipboardOwner() + " ME="
					+ windowHandle);
			success = user32.OpenClipboard(windowHandle);
		}
	}

	public static class WindowsError {
		public int error;
		public String errorMessage;

		public WindowsError(int error, String errorMessage) {
			this.error = error;
			this.errorMessage = errorMessage;
		}
	}

	@Override

	public ClipboardData getClipboardData(ClipboardFormat format) {
		openClipboardWhenFree();
		Pointer p = user32.GetClipboardData(format.getWindows());
		user32.CloseClipboard();
		return clipboardPointerToData(format, p);
	}

	private ClipboardData clipboardPointerToData(ClipboardFormat format, Pointer p) {
		if (p == null) {
			return new EmptyClipboardData(format);
		}
		switch (format.getWindows()) {
		case IWindowsClipboardUser32.CF_TEXT:
			return new PlainTextClipboardData(p.getString(0));
		case IWindowsClipboardUser32.CF_UNICODE:
			// reads properly
			String wideString = p.getWideString(0);
			return new UnicodeClipboardData(wideString);
		default:
			return new UnknownClipboardData(format);
		}
	}
}
