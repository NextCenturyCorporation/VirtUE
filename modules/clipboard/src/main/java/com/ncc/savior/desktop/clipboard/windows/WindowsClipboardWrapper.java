/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.clipboard.windows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.BitMapClipboardData;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.EmptyClipboardData;
import com.ncc.savior.desktop.clipboard.data.FileClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnicodeClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnknownClipboardData;
import com.ncc.savior.util.JavaUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAP;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
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
 * </ol>
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

	// If changing from 32, be aware that windwos seems to expect that scanlines
	// always start at 4 byte boundaries. No code attempts to handle this since we
	// assume each pixel is 4 bytes anyway.
	public static final short BITS_PER_PIXEL = 32;

	static IWindowsClipboardUser32 user32 = IWindowsClipboardUser32.INSTANCE;
	static IWindowsClipboardShell32 shell32 = IWindowsClipboardShell32.INSTANCE;
	static IWindowsClipboardGdi32 gdi32 = IWindowsClipboardGdi32.INSTANCE;
	static Kernel32 kernel32 = Kernel32.INSTANCE;

	WindowProc callback = new WindowProc() {
		@Override
		public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
			// logger.debug("got message(callback)=" + uMsg);
			// For the below, 1 is success where 0 represents failure
			switch (uMsg) {
			case WM_NCCREATE:
				// must return true
				return new LRESULT(1);
			case WM_DESTROYCLIPBOARD:
				// https://msdn.microsoft.com/en-us/library/windows/desktop/ms649024(v=vs.85).aspx
				// docs say success should return 0
				onClipboardEmptied();
				return new LRESULT(0);
			case WM_DRAWCLIPBOARD:
				// no documentation on return value
				onClipboardChanged();
				return new LRESULT(1);
			case WM_RENDERFORMAT:
				onPaste(wParam);
				// docs say this should return 0
				return new LRESULT(0);
			case WM_RENDERALLFORMATS:
				// if processed, which we don't at hte moment, return 0
				return new LRESULT(1);
			case User32.WM_DEVICECHANGE:
				return new LRESULT(1);

			default:
				return new LRESULT(0);
			}
		}
	};
	private HWND windowHandle;
	private IClipboardListener clipboardListener;
	private BlockingQueue<Runnable> runLaterQueue;
	private Pointer data;
	private Thread mainThread;
	protected volatile boolean stopMainThread;

	public WindowsClipboardWrapper(boolean takeClipboard) {
		this.runLaterQueue = new LinkedBlockingQueue<Runnable>();
		Runnable mainRunnable = new Runnable() {

			@Override
			public void run() {
				String className = "delayedRenderClipboardManager";
				String title = "title";
				WNDCLASSEX wx = new WNDCLASSEX();
				wx.clear();
				wx.lpszClassName = className;
				wx.lpfnWndProc = callback;

				while (user32.RegisterClassEx(wx).intValue() == 0) {
					WindowsError error = getLastError();
					logger.error("Error registering class to window: " + error);
				}
				windowHandle = user32.CreateWindowEx(0, className, title, 0, 0, 0, 0, 0, null, null, null, null);
				user32.SetClipboardViewer(windowHandle);

				if (takeClipboard) {
					addToRunLaterQueue(() -> {
						Set<ClipboardFormat> formats = new HashSet<ClipboardFormat>();
						formats.add(ClipboardFormat.TEXT);
						formats.add(ClipboardFormat.UNICODE);
						setDelayedRenderFormats(formats);
					});
				}
				MSG msg = new WinUser.MSG();
				while (!stopMainThread) {
					// get all messages forever
					boolean processedMessage = getMessage(msg);
					if (!processedMessage) {
						// if no message, then run a run later
						Runnable runNow = runLaterQueue.poll();
						if (runNow != null) {
							runNow.run();
						} else {
							// if no run later, then wait a bit and try again
							// however, if we executed something, we want to loop through again to see if
							// there are more
							JavaUtil.sleepAndLogInterruption(10);
						}
					}
				}
				user32.CloseWindow(windowHandle);
				windowHandle = null;
				user32.UnregisterClass(className, Kernel32.INSTANCE.GetModuleHandle(null));
				if (clipboardListener != null) {
					clipboardListener.closed();
				}
			}
		};

		mainThread = new Thread(mainRunnable, "Windows-clipboard-main");
		mainThread.setDaemon(true);
		mainThread.start();
	}

	/**
	 * Sets the windows clipboard into delayed rendering mode for the given formats.
	 * In delayed rendering mode, the first time for each format where an
	 * application attempts to paste, it will callback and ask for the data. We will
	 * handle that callback.
	 */
	@Override
	public void setDelayedRenderFormats(Set<ClipboardFormat> formats) {
		addToRunLaterQueue(new Runnable() {

			@Override
			public void run() {
				writeNullToClipboard(formats);
			}

		});
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
		// retain data to avoid cleanup until we rewrite
		data = clipboardData.createWindowsData(this);
		user32.SetClipboardData(clipboardData.getFormat().getWindows(), data);
		// System owns the data and application should not handle data after the
		// SetClipboardData call. This is explicitly called out in microsoft documents.
		// https://msdn.microsoft.com/en-us/library/windows/desktop/ms649051(v=vs.85).aspx
		if (!clipboardData.isCacheable()) {

			// This call is usually done via windows callback on some unknown thread where
			// the clipboard is accessible for another application. In those cases, the
			// thread needs to be released quickly. Any changes to the clipboard before
			// releasing will occur on the other application's paste. Therefore, we must set
			// the the null for delayed render in our main thread later. We need to wait for
			// the clipboard to become available because at the time of this call, some
			// other application has the clipboard open and is reading. We don't want to
			// write the data until that application is done.
			addToRunLaterQueue(() -> {
				openClipboardWhenFree();
				try {
					writeNullToClipboard(clipboardData.getFormat());
				} finally {
					closeClipboard();
				}
			});
		}
	}

	@Override
	public ClipboardData getClipboardData(ClipboardFormat format) {
		Pointer p = null;
		try {
			openClipboardWhenFree();
			try {
				p = user32.GetClipboardData(format.getWindows());
				return clipboardPointerToData(format, p);
			} catch (Throwable t) {
				logger.error("Error converting clipboard pointer to clipboard data", t);
				return new UnknownClipboardData(format);
			}
		} finally {
			// Moved return inside try/finally so we don't close clipboard until we are done
			// with the data.
			closeClipboard();
		}
	}

	/**
	 * Closing may not be finished when this call is returned. Clipboard listener
	 * should wait for closed callback.
	 */
	@Override
	public void close() throws IOException {
		stopMainThread = true;
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
			Pointer p = new NativelyDeallocatedMemory(1);
			p.setByte(0, (byte) 0);
			user32.SetClipboardData(wParam.intValue(), p);
			logger.error("no listener set!");
			// Memory is now owned by system so we shouldn't clear it
		}
	}

	/**
	 * Called when a local application changes the clipboard.
	 */
	protected void onClipboardChanged() {
		HWND owner = user32.GetClipboardOwner();
		if (!windowHandle.equals(owner)) {
			addToRunLaterQueue(new Runnable() {
				@Override
				public void run() {
					Set<ClipboardFormat> formats = getClipboardFormatsAvailable();
					if (clipboardListener != null) {
						clipboardListener.onClipboardChanged(formats);
					}
				}
			});
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
	protected void writeNullToClipboard(Set<ClipboardFormat> formats) {
		openClipboardWhenFree();
		try {
			boolean success = user32.EmptyClipboard();
			if (!success) {
				throw windowsErrorToException("Error emptying clipboard");
			}
			for (ClipboardFormat format : formats) {
				writeNullToClipboard(format);
			}
		} catch (Throwable t) {
			throw windowsErrorToException("Error attempting to write null to clipboard for formats=" + formats, null,
					t);
		} finally {
			closeClipboard();
		}
	}

	private void addToRunLaterQueue(Runnable runnable) {
		try {
			runLaterQueue.put(runnable);
		} catch (InterruptedException e) {
			// Check implementation of runLaterQueue. It will probably be impossible to
			// block so
			// this is unimportant.
			logger.error("Adding to run later queue was interrupted.  Retrying");
			addToRunLaterQueue(runnable);
		}

	}

	private void closeClipboard() {
		if (!user32.CloseClipboard()) {
			logger.error("### close clipboard failed");
		}
	}

	// private void closeClipboardIfOwner() {
	// HWND owner = user32.GetClipboardOwner();
	// if (windowHandle.equals(owner)) {
	// user32.CloseClipboard();
	// } else {
	// logger.warn("###Clipboard couldn't be closed because we are not owner!");
	// }
	// }

	private void writeNullToClipboard(ClipboardFormat format) {
		user32.SetClipboardData(format.getWindows(), Pointer.NULL);
		WindowsError error = getLastError();
		if (error.error != 0) {
			throw windowsErrorToException("Error writing NULL to clipboard with format=" + format, error);
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

	/**
	 * Returns if a message was received and processed
	 * 
	 * @param msg
	 * @return
	 */
	private boolean getMessage(MSG msg) {
		boolean hasMessage = user32.PeekMessage(msg, windowHandle, 0, 0, 1);
		if (hasMessage) {
			// user32.GetMessage(msg, windowHandle, 0, 0);
			if (msg.message != 0xC228) {
			}
			// user32.TranslateMessage(msg);
			user32.DispatchMessage(msg);
		}
		return hasMessage;
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
		WindowsError winError = new WindowsError(error, lpBuffer.getValue().getWideString(0));
		return winError;
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
			logger.debug("clipboard unable to be opened.  Trying again.  Owner=" + user32.GetClipboardOwner() + " ME="
					+ windowHandle);
			// Error doesn't seem to work here. always returned 0, operation returned
			// successfully
			// WindowsError error = getLastError();
			// logger.debug("Error: " + error);
			JavaUtil.sleepAndLogInterruption(10);
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

		@Override
		public String toString() {
			return "WindowsError [error=" + error + ", errorMessage=" + errorMessage + "]";
		}
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
		case IWindowsClipboardUser32.CF_HDROP:
			List<File> files = new ArrayList<File>();
			int numFiles = shell32.DragQueryFileA(p, IWindowsClipboardShell32.DRAG_QUERY_GET_NUM_FILES_INDEX,
					Pointer.NULL, 0);
			logger.debug(numFiles + " files copied.");
			for (int i = 0; i < numFiles; i++) {
				int charactersNeeded = shell32.DragQueryFileA(p, i, Pointer.NULL, 0);
				// Structure t = StructStgMedium.newInstance(StructStgMedium.class, p);
				int memSize = charactersNeeded + 1;
				Memory memory = new Memory(memSize);
				int success = shell32.DragQueryFileA(p, i, memory, memSize);
				if (success == 0) {
					logger.warn("Failed to copy file with index=" + i);
				} else {
					String filePath = memory.getString(0);
					logger.debug("File to be copied" + filePath);
					File file = new File(filePath);
					if (file.exists()) {
						files.add(file);
					} else {
						logger.warn("File to be copied at '" + file.getAbsolutePath() + "' does not exist");
					}
				}
			}
			return new FileClipboardData(files);
		case IWindowsClipboardUser32.CF_BITMAT:
			// convertPngToPngData(p);
			try {
				return convertBitmapPngData(p);
			} catch (IOException e) {
				logger.debug("error writing image into PNG format!", e);
				return new EmptyClipboardData(format);
			}
		default:
			return new UnknownClipboardData(format);
		}
	}

	/**
	 * Notes about Microsofts quirks:
	 * <ol>
	 * <li>PNG Option - Although BITMAPINFOHEADER has an option for compression as
	 * BI_PNG, that isn't really a valid format for the clipboard or windows itself.
	 * It is intended for sending images to printers which have hardware support for
	 * png.
	 * <li>Transparency - Windows clipboard bitmap does not support transparency.
	 * 
	 * @param p
	 * @return
	 * @throws IOException
	 */
	private BitMapClipboardData convertBitmapPngData(Pointer p) throws IOException {
		HBITMAP hbitmap = new HBITMAP(p);

		BITMAP gdiBitMap = new BITMAP();
		gdi32.GetObject(hbitmap, gdiBitMap.size(), gdiBitMap.getPointer());
		gdiBitMap.autoRead();
		BITMAPINFO info = getBitmapInfo(gdiBitMap.bmWidth.intValue(), gdiBitMap.bmHeight.intValue(), BITS_PER_PIXEL);
		HDC hDC = user32.GetDC(windowHandle);
		int sizeBytes = info.bmiHeader.biWidth * info.bmiHeader.biHeight * 4;
		Pointer lpvBits = new Memory(sizeBytes);
		int scanLines = gdi32.GetDIBits(hDC, hbitmap, 0, gdiBitMap.bmHeight.intValue(), lpvBits, info,
				WinGDI.DIB_RGB_COLORS);
		if (scanLines != gdiBitMap.bmHeight.intValue()) {
			logger.warn("Warning! Windows bitmap conversion did not read the expected amount of lines.  linesRead="
					+ scanLines + " ExpectedImageHeight=" + gdiBitMap.bmHeight.intValue());
		}
		BufferedImage image = new BufferedImage(info.bmiHeader.biWidth, info.bmiHeader.biHeight,
				BufferedImage.TYPE_INT_ARGB);
		int i = 0;
		int bytesPerPixel = BITS_PER_PIXEL / 8;
		for (int y = image.getHeight() - 1; y >= 0; y--) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = 0;
				int r = lpvBits.getByte(i + 2);
				int g = lpvBits.getByte(i + 1);
				int b = lpvBits.getByte(i);
				// int a = lpvBits.getByte(i + 3);
				// a &= 0xFF;
				r &= 0xFF;
				g &= 0xFF;
				b &= 0xFF;
				int a = 0xFF;
				rgb = (a << 24) | (r << 16) | (g << 8) | (b << 0);
				// logger.debug("RGH hex=" + Integer.toHexString(rgb) + " r=" +
				// Integer.toHexString(r) + " g="
				// + Integer.toHexString(g) + " b=" + Integer.toHexString(b) + " a=" +
				// Integer.toHexString(a));
				image.setRGB(x, y, rgb);
				i += bytesPerPixel;
			}
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", output);
		return new BitMapClipboardData(output.toByteArray());
	}

	public static BITMAPINFO getBitmapInfo(int width, int height, short bitsPerPixel) {
		BITMAPINFO info = new BITMAPINFO();
		info.bmiHeader.biSize = info.size();
		info.bmiHeader.biWidth = width;
		info.bmiHeader.biHeight = height;
		info.bmiHeader.biPlanes = 1;
		info.bmiHeader.biBitCount = bitsPerPixel;
		info.bmiHeader.biCompression = WinGDI.BI_RGB;
		info.autoWrite();
		return info;
	}

	public IWindowsClipboardUser32 getUser32() {
		return user32;
	}

	public HWND getWindowHandle() {
		return windowHandle;
	}

	public IWindowsClipboardGdi32 getGdi32() {
		return gdi32;
	}
}
