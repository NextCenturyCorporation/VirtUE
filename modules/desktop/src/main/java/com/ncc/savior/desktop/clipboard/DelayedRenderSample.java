package com.ncc.savior.desktop.clipboard;

import java.util.Arrays;

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
public class DelayedRenderSample {
	private static Logger logger = LoggerFactory.getLogger(DelayedRenderSample.class);
	private static boolean clipboardNotUsed = true;

	public static void main(String[] args) throws InterruptedException {

		WString className = new WString("myclass");
		HWND windowHandle = null;
		String title = "title";
		WNDCLASSEX wx = new WNDCLASSEX();
		wx.clear();
		wx.lpszClassName = className;
		wx.lpfnWndProc = callback;

		if (User32.INSTANCE.RegisterClassEx(wx).intValue() != 0) {
			windowHandle = user32.CreateWindowEx(0, className, title, 0, 0, 0, 0, 0, null, null, null, null);
			// user32.SetClipboardViewer(windowHandle);
		}

		writeStringToClipboard(null, windowHandle, true);
		// printClipboardFormatsAvailable();

		while (clipboardNotUsed) {
			getMessage(windowHandle);
			Thread.sleep(100);
		}
	}

	private static void printClipboardFormatsAvailable() {
		IntByReference returnedSizeOfFormats = new IntByReference();
		int sizeOfFormats = 20;
		int[] formats = new int[sizeOfFormats];
		boolean success = user32.GetUpdatedClipboardFormats(formats, sizeOfFormats, returnedSizeOfFormats);
		if (success) {
			logger.debug(Arrays.toString(Arrays.copyOf(formats, returnedSizeOfFormats.getValue())));
		} else {
			logger.error("Error getting clipboard formats");
		}

	}

	private static boolean writeStringToClipboard(String myString, final HWND windowHandle, boolean normalString)
			throws InterruptedException {
		boolean success = user32.OpenClipboard(windowHandle);
		printLastError("open clipboard", false);
		logger.debug("previous clipboard owner:" + user32.GetClipboardOwner());
		logger.debug("clipboard opened: " + success);
		success = user32.EmptyClipboard();
		logger.debug("clipboard emptied: " + success);
		// StringByReference sbr = new StringByReference(myString);
		// Pointer m = sbr.getPointer();
		try {
			return putClipboardStringDirectly(myString, normalString);
		} catch (Throwable t) {
			printLastError("error setting clipboard", true);
			logger.debug("Error setting clipboard", t);
		} finally {
			success = user32.CloseClipboard();
			logger.debug("clipboard closed: " + success);
			logger.debug("clipboard owner:" + user32.GetClipboardOwner());
		}
		return false;
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
		if (p == null) {
			printLastError("set clipboard", true);
		} else {
			logger.debug("Clipboard set with pointer returned:" + p);
		}
		return (p != null);
	}

	private static void printLastError(String prefix, boolean printOnSuccess) {
		int error = kernel32.GetLastError();
		int langId = 0;
		PointerByReference lpBuffer = new PointerByReference();
		int ret = kernel32.FormatMessage(WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER | WinBase.FORMAT_MESSAGE_FROM_SYSTEM
				| WinBase.FORMAT_MESSAGE_IGNORE_INSERTS, null, error, langId, lpBuffer, 0, null);
		if (printOnSuccess || error != 0) {
			logger.error(prefix + " error=" + error + " " + lpBuffer.getValue().getWideString(0));
		}
	}

	private static void getMessage(HWND windowHandle) {

		MSG msg = new WinUser.MSG();
		boolean hasMessage = user32.PeekMessage(msg, windowHandle, 0, 0, 0);
		printLastError("PeekMessage", false);
		// logger.debug(inst.toString(true));
		if (hasMessage) {
			int i = user32.GetMessage(msg, windowHandle, 0, 0);
			if (msg.message != 0xC228) {
				user32.TranslateMessage(msg);
				LRESULT result = user32.DispatchMessage(msg);
				logger.debug("get message i=" + i);
				logger.debug(msg.toString(false));
				logger.debug("MSG#" + msg.message);
				printLastError("GetMessage", false);
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

	static WindowProc callback = new WindowProc() {
		@Override
		public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
			logger.debug("Message received: " + uMsg);
			switch (uMsg) {
			case WM_NCCREATE:
				return new LRESULT(1);
			case WM_DESTROYCLIPBOARD:
				logger.debug("clipboard control lost " + hWnd);
				return new LRESULT(1);
			case WM_DRAWCLIPBOARD:
				logger.debug("clipboard changed " + hWnd);
				return new LRESULT(1);
			case WM_RENDERFORMAT:
				logger.debug("RENDER FORMAT!!");
				putClipboardStringDirectly("IT WORKED", true);
				clipboardNotUsed = false;
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
}
