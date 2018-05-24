package com.ncc.savior.desktop.clipboard.linux;

import java.util.Collection;
import java.util.Date;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.util.JavaUtil;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Atom;
import com.sun.jna.platform.unix.X11.AtomByReference;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.unix.X11.XErrorEvent;
import com.sun.jna.platform.unix.X11.XErrorHandler;
import com.sun.jna.platform.unix.X11.XEvent;
import com.sun.jna.platform.unix.X11.XSelectionClearEvent;
import com.sun.jna.platform.unix.X11.XSelectionEvent;
import com.sun.jna.platform.unix.X11.XSelectionRequestEvent;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

public class X11ClipboardWrapper implements IClipboardWrapper {

	@Override
	public void setDelayedRenderFormats(Collection<Integer> formats) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setClipboardListener(IClipboardListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDelayedRenderData(ClipboardData clipboardData) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClipboardData getClipboardData(int format) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		ILinuxClipboardX11 x11 = ILinuxClipboardX11.INSTANCE;
		Display display = X11.INSTANCE.XOpenDisplay(null);
		System.out.println("mydisplay:" + display);

		Window window = X11.INSTANCE.XDefaultRootWindow(display);

		XEvent event = new XEvent();
		// x11.XNextEvent(display, event_return);

		XErrorHandler handler = new XErrorHandler() {

			@Override
			public int apply(Display display, XErrorEvent errorEvent) {
				byte[] buffer = new byte[2048];
				x11.XGetErrorText(display, errorEvent.error_code, buffer, 2048);
				System.out.println(new String(buffer));
				return 1;
			}
		};
		x11.XSetErrorHandler(handler);

		String format1 = "UTF_STRING";
		String format2 = "STRING";
		printSelection(x11, display, window, format1);
		printSelection(x11, display, window, format2);
		return;
	}

	public static void other(ILinuxClipboardX11 x11, Display display, Window window) {
		Atom clipboard_atom = x11.XInternAtom(display, "CLIPBOARD", false);
		Atom utf8 = x11.XInternAtom(display, "UTF8_STRING", false);
		Atom penguin = x11.XInternAtom(display, "Penguin", false);
		Window retWin = x11.XGetSelectionOwner(display, clipboard_atom);
		System.out.println("Me=" + window + " clipboard owner=" + retWin);

		long currentTime = 0l;

		x11.XSetSelectionOwner(display, clipboard_atom, window, new NativeLong(currentTime));
		// retWin = x11.XGetSelectionOwner(display, clipboard_atom);
		// System.out.println("Me=" + window + " clipboard owner=" + retWin + " time=" +
		// currentTime);
		// I believe XConvertSelection requests the selection
		// XConvertSelection - requests that the specified selection be converted to the
		// specified target type:
		// x11.XConvertSelection(display, clipboard_atom, utf8, penguin, window, new
		// NativeLong(0));
		// retWin = x11.XGetSelectionOwner(display, clipboard_atom);

		System.out.println("Me=" + window + " clipboard owner=" + retWin);

		for (int i = 0; i < 5000; i++) {
			// System.out.println("#Events: " + x11.XEventsQueued(display, 0));
			XEvent peekEvent = new XEvent();
			int peekRet = x11.XPeekEvent(display, peekEvent);
			// System.out.println("Peek: "+peekRet+" "+peekEvent);
			if (peekRet != 0) {
				XEvent event2 = new XEvent();
				int eventReturn = x11.XNextEvent(display, event2);
				System.out.println("eventReturn: " + eventReturn);
				handleEvent(x11, event2, display);
			} else {
			}
			JavaUtil.sleepAndLogInterruption(100);
		}
	}

	// see
	// https://stackoverflow.com/questions/27378318/c-get-string-from-clipboard-on-linux/44992938#44992938
	private static void printSelection(ILinuxClipboardX11 x11, Display display, Window window, String format) {
		Atom clipboard = x11.XInternAtom(display, "CLIPBOARD", false);
		Atom formatAtom = x11.XInternAtom(display, format, false);
		Atom property = x11.XInternAtom(display, "XSEL_DATA", false);
		x11.XConvertSelection(display, clipboard, formatAtom, property, window, new NativeLong(0));
		XEvent event = new XEvent();
		// do {
		// x11.XNextEvent(display, event);
		// } while (event.type != X11.SelectionNotify ||
		// event.xselection.selection.equals(clipboard));
		if (event.xselection.property != null || true) {
			// TODO review this
			long propSize = 4096 * 2 / 4;
			boolean delete = false;
			AtomByReference actualTypeReturn = new AtomByReference();
			IntByReference actualFormatReturn = new IntByReference();
			NativeLongByReference nItemsReturn = new NativeLongByReference();
			NativeLongByReference bytesAfterReturn = new NativeLongByReference();
			PointerByReference propReturn = new PointerByReference();
			Atom anyPropAtom = new Atom(X11.AnyPropertyType);
			System.out.println("prior");
			int ret = x11.XGetWindowProperty(display, window, property, new NativeLong(0), new NativeLong(propSize),
					delete, anyPropAtom, actualTypeReturn, actualFormatReturn, nItemsReturn, bytesAfterReturn,
					propReturn);
			if (ret != X11.Success) {
				System.out.println("UNSUCCESSFUL");

			} else {
				System.out.println("ret=" + ret);
				System.out.println(actualTypeReturn.getValue());
				System.out.println(actualFormatReturn.getValue());
				System.out.println(nItemsReturn.getValue());
				System.out.println(bytesAfterReturn.getValue());
				System.out.println(propReturn.getValue());

				System.out.println("String: " + propReturn.getValue().getString(0));
			}

			/*
			 * x11.XGetWintdowProperty(display, window, property, 0, LONG_MAX/4, True,
			 * AnyPropertyType, &fmtid, &resbits, &ressize, &restail, (unsigned
			 * char**)&result); if (fmtid != incrid) { printf("%.*s", (int)ressize, result);
			 * } XFree(result);
			 *
			 * if (fmtid == incrid) { do { do { XNextEvent(display, &event); } while
			 * (event.type != PropertyNotify || event.xproperty.atom != propid ||
			 * event.xproperty.state != PropertyNewValue);
			 *
			 * XGetWindowProperty(display, window, propid, 0, LONG_MAX/4, True,
			 * AnyPropertyType, &fmtid, &resbits, &ressize, &restail, (unsigned
			 * char**)&result); printf("%.*s", (int)ressize, result); XFree(result); } while
			 * (ressize > 0); }
			 *
			 * return True; } else { return False; }
			 */
		}

	}

	private static void handleEvent(ILinuxClipboardX11 x11, XEvent event, Display display) {
		System.out.println("eventType=" + event.type);
		switch (event.type) {
		case X11.SelectionRequest:
			XSelectionRequestEvent sre = (XSelectionRequestEvent) XEvent.newInstance(XSelectionRequestEvent.class,
					event.getPointer());
			sre.autoRead();
			XSelectionEvent se = new X11.XSelectionEvent();
			se.display = sre.display;
			se.property = sre.property;
			se.requestor = sre.requestor;
			// selection or clipboard name (clipboard)
			se.selection = sre.selection;
			// type or format
			se.target = sre.target;
			Atom penguin = x11.XInternAtom(display, "Penguin", false);
			se.property = penguin;
			se.type = X11.SelectionNotify;
			int propagate = 0;
			XEvent sendEvent = new XEvent();
			se.autoWrite();
			sendEvent.setTypedValue(se);
			int sendRet = x11.XSendEvent(display, sre.requestor, propagate, new NativeLong(X11.NoEventMask), sendEvent);
			System.out.println("SelectionRequestEvent received.  Sent event with ret=" + sendRet + " " + se);
			break;
		case X11.SelectionClear:
			XSelectionClearEvent sce = (XSelectionClearEvent) XEvent.newInstance(XSelectionClearEvent.class,
					event.getPointer());
			System.out.println("selection cleared " + sce);
			break;
		case X11.SelectionNotify:
			XSelectionEvent sne = (XSelectionEvent) XEvent.newInstance(XSelectionEvent.class, event.getPointer());
			System.out.println("selection notified " + sne);
			break;
		default:
			System.out.println("got event " + event.type);
		}

	}

	private static NativeLong getNow() {
		return new NativeLong(new Date().getTime());
	}

	private static NativeLong getNow(int i) {
		return new NativeLong(new Date().getTime() + i);
	}

}
