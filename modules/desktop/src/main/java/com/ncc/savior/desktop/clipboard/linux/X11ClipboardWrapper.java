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

	private static boolean selectionAvailable;
	private ILinuxClipboardX11 x11;
	private Display display;
	private Window window;
	private Atom clipboardAtom;
	private Atom selectionDataProperty;

	public X11ClipboardWrapper() {
		x11 = ILinuxClipboardX11.INSTANCE;
		display = X11.INSTANCE.XOpenDisplay(null);
		window = X11.INSTANCE.XDefaultRootWindow(display);
		System.out.println("MyWindow: " + window);
		clipboardAtom = x11.XInternAtom(display, "CLIPBOARD", false);
		selectionDataProperty = x11.XInternAtom(display, "XSEL_DATA", false);

		NativeLong eventMask = new NativeLong(x11.LockMask);
		x11.XSelectInput(display, window, eventMask);
		XErrorHandler handler = new XErrorHandler() {

			@Override
			public int apply(Display display, XErrorEvent errorEvent) {
				byte[] buffer = new byte[2048];
				x11.XGetErrorText(display, errorEvent.error_code, buffer, 2048);
				System.out.println("ERROR: " + new String(buffer));
				return 1;
			}
		};
		x11.XSetErrorHandler(handler);
	}

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

	public boolean amISelectionOwner() {
		Window retWin = x11.XGetSelectionOwner(display, clipboardAtom);
		return window.equals(retWin);
	}

	public boolean isThereAClipboardOwner() {
		Window retWin = x11.XGetSelectionOwner(display, clipboardAtom);
		return retWin != null;
	}

	public void becomeSelectionOwner() {
		x11.XSetSelectionOwner(display, clipboardAtom, window, new NativeLong(0l));
	}

	public static void main(String[] args) {
		X11ClipboardWrapper wrapper = new X11ClipboardWrapper();
		// System.out.println("IAmOwner=" + wrapper.amISelectionOwner());
		// wrapper.becomeSelectionOwner();
		// System.out.println("IAmOwner=" + wrapper.amISelectionOwner());
		// XEvent event = wrapper.getNextEventWithTimeout(2000, 100);
		// if (event != null) {
		// System.out.println("Event received! " + event.type);
		// }

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				String format1 = "UTF_STRING";
				String format2 = "STRING";
				while (true) {
					wrapper.printSelection(format2);
					JavaUtil.sleepAndLogInterruption(1000);
				}
			}

		});
		t.start();
		while (true) {
			XEvent e = wrapper.getNextEventWithTimeout(1000, 100);
			System.out.println("EVENT: " + e);
			if (e.type == X11.SelectionNotify) {
				selectionAvailable = true;
			}
		}

		// wrapper.printSelection(format2, null);
	}

	/**
	 * returns null if no event.
	 *
	 * @param timeoutMillis
	 * @param intervalMillis
	 * @return
	 */
	public XEvent getNextEventWithTimeout(long timeoutMillis, int intervalMillis) {
		long stopTime = System.currentTimeMillis() + timeoutMillis;
		while (System.currentTimeMillis() < stopTime) {
			XEvent peekEvent = new XEvent();
			// System.out.println("peeking");
			int peekRet = x11.XPeekEvent(display, peekEvent);
			// System.out.println("Peek Event=" + peekRet);
			if (peekRet != 0) {
				XEvent event = blockForEvent();
				return event;
			}
			JavaUtil.sleepAndLogInterruption(intervalMillis);
		}
		return null;
	}

	private XEvent blockForEvent() {
		XEvent event = new XEvent();
		int eventReturn = x11.XNextEvent(display, event);
		if (eventReturn == X11.Success) {
			return event;
		} else {
			throw new RuntimeException("Get next event failed");
		}
	}

	private void convertSelectionAndSync(String format) {
		String selection = "CLIPBOARD";
		Atom clipboard = x11.XInternAtom(display, selection, false);
		Atom formatAtom = x11.XInternAtom(display, format, false);
		x11.XConvertSelection(display, clipboard, formatAtom, selectionDataProperty, window, new NativeLong(0));
		x11.XSync(display, false);
	}

	// see
	// https://stackoverflow.com/questions/27378318/c-get-string-from-clipboard-on-linux/44992938#44992938
	private void printSelection(String format) {
		Atom increment = x11.XInternAtom(display, "INCR", false);
		// default selection is CLIPBOARD
		convertSelectionAndSync(format);
		System.out.println("requested format");
//		XEvent event = null;
//		event = getNextEventWithTimeout(3000, 100);
//		event.autoRead();
//		while (event.type != X11.SelectionNotify) {
//			System.out.println("event type=" + event.type);
//			if (event.type == X11.PropertyNotify) {
//				XPropertyEvent propEvent = (XPropertyEvent) event.getTypedValue(XPropertyEvent.class);
//				propEvent.autoRead();
//				System.out.println(propEvent + "  " + propEvent.atom);
//			}
//			event = getNextEventWithTimeout(2000, 100);
//		}
		while (!selectionAvailable) {
			JavaUtil.sleepAndLogInterruption(100);
		}
//		System.out.println("event type=" + event.type);
//		XSelectionEvent selEvent = (XSelectionEvent) event.getTypedValue(XSelectionEvent.class);
//		selEvent.autoRead();
		// TODO review this
		long propSize = 4096000 * 2 / 4;
		boolean delete = false;
		AtomByReference actualTypeReturn = new AtomByReference();
		IntByReference actualFormatReturn = new IntByReference();
		NativeLongByReference nItemsReturn = new NativeLongByReference();
		NativeLongByReference bytesAfterReturn = new NativeLongByReference();
		PointerByReference propReturn = new PointerByReference();
		Atom anyPropAtom = new Atom(X11.AnyPropertyType);
		System.out.println("prior");
		int ret = x11.XGetWindowProperty(display, window, selectionDataProperty, new NativeLong(0),
				new NativeLong(propSize), delete,
				anyPropAtom, actualTypeReturn, actualFormatReturn, nItemsReturn, bytesAfterReturn, propReturn);
		if (ret != X11.Success) {
			System.out.println("UNSUCCESSFUL");
		} else {
			System.out.println("ret=" + ret);
			System.out.println("Type: " + actualTypeReturn.getValue());
			System.out.println("Format: " + actualFormatReturn.getValue() + "  Incr: " + increment);
			System.out.println("nItems: " + nItemsReturn.getValue());
			System.out.println("bytesAfter: " + bytesAfterReturn.getValue());
			// System.out.println(propReturn.getValue());

			String data = propReturn.getValue().getString(0);
			int maxLengthToPrint = 64;
			System.out.println("String: " + data.length() + " - "
					+ (data.length() > maxLengthToPrint ? data.substring(0, maxLengthToPrint) : data));
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
