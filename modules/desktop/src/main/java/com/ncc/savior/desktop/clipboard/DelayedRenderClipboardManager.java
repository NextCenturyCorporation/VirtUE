package com.ncc.savior.desktop.clipboard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
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
import com.sun.jna.win32.W32APIOptions;

/**
 * In order to get the Delayed Render to work with JNA, it seems you need the
 * following steps.
 * <ol>
 * <li>Create a window where we can edit the Window Proc and handle
 * WM_RENDERFORMAT and WM_RENDERALLFORMATS
 * <li>Write null to the clipboard using the entire process (Open, Empty, write,
 * close). Make sure the window handle is passed to the open function.
 * <li>Have a loop to clear the message queue via GetMessage or PeekMessage.
 *
 *
 */
public class DelayedRenderClipboardManager {
	private static Logger logger = LoggerFactory.getLogger(DelayedRenderClipboardManager.class);
	private HWND windowHandle;
	private ScheduledExecutorService executor;
	protected String clipboardBuffer;

	public DelayedRenderClipboardManager() throws InterruptedException {
		WString className = new WString("delayedRenderClipboardManager");
		String title = "title";
		WNDCLASSEX wx = new WNDCLASSEX();
		wx.clear();
		wx.lpszClassName = className;
		wx.lpfnWndProc = callback;

		if (User32.INSTANCE.RegisterClassEx(wx).intValue() != 0) {
			windowHandle = user32.CreateWindowEx(0, className, title, 0, 0, 0, 0, 0, null, null, null, null);
			user32.SetClipboardViewer(windowHandle);
		}

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
		executor = Executors.newScheduledThreadPool(1, threadFactory);
		getClipboardAndTakeControl();
	}

	private void getClipboardAndTakeControl() throws InterruptedException {
		Set<Integer> formats = getClipboardFormatsAvailable();
		if (formats.contains(1)) {
			this.clipboardBuffer = getStringClipboardNative();
		}
		writeStringToClipboard(null, windowHandle, true);
	}

	private static String getStringClipboardNative() {
		// logger.debug("cb owner:" + user32.GetClipboardOwner());
		user32.OpenClipboard(null);
		Pointer p = user32.GetClipboardData(MyUser32.CF_TEXT);
		user32.CloseClipboard();
		if (p == null) {
			return null;
		} else {
			return p.getString(0);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		DelayedRenderClipboardManager clipboardManager = new DelayedRenderClipboardManager();
		HWND wh = clipboardManager.getWindowHandle();

		while (true) {
			getMessage(wh);
			Thread.sleep(10);
		}
	}

	private HWND getWindowHandle() {
		return windowHandle;
	}

	private static Set<Integer> getClipboardFormatsAvailable() {
		IntByReference returnedSizeOfFormats = new IntByReference();
		int sizeOfFormats = 20;
		int[] formats = new int[sizeOfFormats];
		boolean success = user32.GetUpdatedClipboardFormats(formats, sizeOfFormats, returnedSizeOfFormats);
		if (success) {
			Set<Integer> set = new HashSet<Integer>();
			int[] arr = Arrays.copyOf(formats, returnedSizeOfFormats.getValue());
			for (int a : arr) {
				set.add(a);
			}
			return set;
		} else {
			throw new RuntimeException(getLastError("Error getting clipboard formats.", false));
		}

	}

	private static boolean writeStringToClipboard(String myString, final HWND windowHandle, boolean normalString)
			throws InterruptedException {
		boolean success = user32.OpenClipboard(windowHandle);
		if (!success) {
			throw new RuntimeException(getLastError("OpenClipboard Error", true));
		}
		success = user32.EmptyClipboard();
		if (!success) {
			throw new RuntimeException(getLastError("EmptyClipboard Error", true));
		}
		try {
			return putClipboardStringDirectly(myString, normalString);
		} catch (Throwable t) {
			throw new RuntimeException(getLastError("error setting clipboard", true), t);
		} finally {
			success = user32.CloseClipboard();
			if (!success) {
				throw new RuntimeException(getLastError("CloseClipboard Error", true));
			}
		}
	}

	private static boolean putClipboardStringDirectly(String myString, boolean normalString) {
		Pointer pnt;
		if (myString == null) {
			pnt = Pointer.NULL;
			// Memory m = new Memory(1);
			// m.clear();
			// m.setByte(0, (byte) 0);
			// pnt = m;
		} else {
			Memory m = new Memory(4 * (myString.getBytes().length + 1));
			m.clear();
			if (normalString) {
				m.setString(0, myString);
			} else {
				m.setWideString(0, myString);
			}
			pnt = m;
		}

		Pointer p;
		if (normalString) {
			p = user32.SetClipboardData(MyUser32.CF_TEXT, pnt);
		} else {
			p = user32.SetClipboardData(MyUser32.CF_UNICODE, pnt);
		}
		if (myString != null && p == null) {
			throw new RuntimeException(getLastError("set clipboard", true));
		}
		return (p != null);
	}

	private static String getLastError(String prefix, boolean printOnSuccess) {
		int error = kernel32.GetLastError();
		int langId = 0;
		PointerByReference lpBuffer = new PointerByReference();
		int ret = kernel32.FormatMessage(WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER | WinBase.FORMAT_MESSAGE_FROM_SYSTEM
				| WinBase.FORMAT_MESSAGE_IGNORE_INSERTS, null, error, langId, lpBuffer, 0, null);
		if (printOnSuccess || error != 0) {
			return prefix + " error=" + error + " " + lpBuffer.getValue().getWideString(0);
		}
		return null;
	}

	private static void getMessage(HWND windowHandle) {
		MSG msg = new WinUser.MSG();
		boolean hasMessage = user32.PeekMessage(msg, windowHandle, 0, 0, 0);
		if (hasMessage) {
			user32.GetMessage(msg, windowHandle, 0, 0);
			if (msg.message != 0xC228) {
				user32.TranslateMessage(msg);
				user32.DispatchMessage(msg);
				logger.debug(msg.toString(false));
			}
		}
	}

	public interface MyUser32 extends User32 {
		// DEFAULT_OPTIONS is critical for W32 API functions to simplify ASCII/UNICODE
		// details
		MyUser32 INSTANCE = (MyUser32) Native.loadLibrary("user32", MyUser32.class, W32APIOptions.DEFAULT_OPTIONS);
		int CF_TEXT = 1;
		int CF_UNICODE = 13;

		boolean EmptyClipboard();

		boolean OpenClipboard(WinDef.HWND window);

		boolean CloseClipboard();

		WinDef.HWND GetClipboardOwner();

		Pointer SetClipboardData(int format, Pointer data);

		Pointer GetClipboardData(int format);

		int EnumClipboardFormats(int format);

		boolean GetUpdatedClipboardFormats(int[] formats, int sizeOfFormats, IntByReference returnedSizeOfFormats);

		WinDef.HWND SetClipboardViewer(WinDef.HWND handle);
	}

	static MyUser32 user32 = MyUser32.INSTANCE;
	static Kernel32 kernel32 = Kernel32.INSTANCE;
	static final int WM_NCCREATE = 0x0081;

	protected static final int WM_DESTROYCLIPBOARD = 0x0307;
	protected static final int WM_DRAWCLIPBOARD = 0x0308;
	protected static final int WM_RENDERFORMAT = 0x0305;
	protected static final int WM_RENDERALLFORMATS = 0x0306;

	WindowProc callback = new WindowProc() {
		@Override
		public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
			switch (uMsg) {
			case WM_NCCREATE:
				return new LRESULT(1);
			case WM_DESTROYCLIPBOARD:
				logger.debug("clipboard control lost " + hWnd);
				onClipboardTaken();
				return new LRESULT(1);
			case WM_DRAWCLIPBOARD:
				logger.debug("clipboard changed " + hWnd);
				onClipboardTaken();
				return new LRESULT(1);
			case WM_RENDERFORMAT:
				logger.debug("RENDER FORMAT!!");
				putClipboardStringDirectly(clipboardBuffer, true);
				onClipboardPasted();
				return new LRESULT(1);
			case WM_RENDERALLFORMATS:
				logger.debug("RENDER ALL FORMAT!!");
				return new LRESULT(1);
			case User32.WM_DEVICECHANGE:
				return new LRESULT(1);

			default:
				return new LRESULT(0);
			}
		}
	};

	protected void onClipboardPasted() {
		executor.schedule(new Runnable() {

			@Override
			public void run() {
				try {
					// TODO probably need to make sure clipboard is available to be opened.
					writeStringToClipboard(null, windowHandle, true);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}, 5, TimeUnit.MILLISECONDS);

	}

	protected void onClipboardTaken() {
		try {
			HWND owner = user32.GetClipboardOwner();
			if (owner == null || !owner.equals(windowHandle)) {
				getClipboardAndTakeControl();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
