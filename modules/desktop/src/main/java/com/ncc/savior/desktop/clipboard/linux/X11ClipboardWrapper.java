package com.ncc.savior.desktop.clipboard.linux;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.EmptyClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnknownClipboardData;
import com.ncc.savior.desktop.clipboard.data.WideTextClipboardData;
import com.ncc.savior.util.JavaUtil;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Atom;
import com.sun.jna.platform.unix.X11.AtomByReference;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.unix.X11.XErrorEvent;
import com.sun.jna.platform.unix.X11.XErrorHandler;
import com.sun.jna.platform.unix.X11.XEvent;
import com.sun.jna.platform.unix.X11.XPropertyEvent;
import com.sun.jna.platform.unix.X11.XSelectionClearEvent;
import com.sun.jna.platform.unix.X11.XSelectionEvent;
import com.sun.jna.platform.unix.X11.XSelectionRequestEvent;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

public class X11ClipboardWrapper implements IClipboardWrapper {
	private static final Logger logger = LoggerFactory.getLogger(X11ClipboardWrapper.class);

	private static boolean selectionAvailable;
	private ILinuxClipboardX11 x11;
	private Display display;
	private Window window;
	private Atom clipboardAtom;
	private Atom selectionDataProperty;

	public X11ClipboardWrapper() {
		x11 = ILinuxClipboardX11.INSTANCE;
		display = X11.INSTANCE.XOpenDisplay(null);
		int screen = x11.XDefaultScreen(display);
		Window defaultWindow = X11.INSTANCE.XRootWindow(display, screen);
		window = x11.XCreateSimpleWindow(display, defaultWindow, 0, 0, 1, 1, 0, 1, 1);
		System.out.println("MyWindow: " + window);
		clipboardAtom = x11.XInternAtom(display, "CLIPBOARD", false);
		selectionDataProperty = x11.XInternAtom(display, "XSEL_DATA", false);

		NativeLong eventMask = new NativeLong(X11.PropertyChangeMask);
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
	public void setDelayedRenderFormats(Collection<ClipboardFormat> formats) {
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
	public ClipboardData getClipboardData(ClipboardFormat format) {
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
		x11.XSetSelectionOwner(display, clipboardAtom, window, new NativeLong(X11.CurrentTime));
	}

	public static void main(String[] args) {
		singleThreadedStart();
	}

	public static void singleThreadedStart() {
		X11ClipboardWrapper wrapper = new X11ClipboardWrapper();
		String format1 = "UTF_STRING";
		String format2 = "STRING";

		while (true) {
			wrapper.setClipboardDataString("test");
			JavaUtil.sleepAndLogInterruption(5000);

			ClipboardData data = wrapper.getClipboardDataInternal(format2);
			// wrapper.printSelection(format2, true);
			logger.debug(data.toString());
			JavaUtil.sleepAndLogInterruption(5000);
		}

	}

	public static void threadedStart() {
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
					wrapper.printSelection(format2, false);
					JavaUtil.sleepAndLogInterruption(1000);
				}
			}

		});
		t.start();
		while (true) {
			XEvent e = wrapper.getNextEventWithTimeout(1000, 100);
			if (e != null) {
				System.out.println("EVENT: " + e);
				if (e != null && e.type == X11.SelectionNotify) {
					selectionAvailable = true;
				}
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
			boolean peekRet = x11.XCheckWindowEvent(display, window, new NativeLong(X11.PropertyChangeMask), peekEvent);
			// boolean peekRet = x11.XPeekEvent(display, peekEvent) == X11.Success;
			// System.out.println("Peek Event=" + peekRet);
			if (peekRet) {
				XEvent event = blockForEvent();
				return event;
			}
			JavaUtil.sleepAndLogInterruption(intervalMillis);
		}
		return null;
	}

	private void setClipboardDataString(String data) {
		x11.XSetSelectionOwner(display, clipboardAtom, window, new NativeLong(X11.CurrentTime));
		Window owner = x11.XGetSelectionOwner(display, clipboardAtom);
		if (!window.equals(owner)) {
			logger.warn("Failed to take clipboard ownership");
			return;
		}
		XEvent event = new XEvent();
		while (event.type != X11.SelectionClear) {
			x11.XNextEvent(display, event);
			switch (event.type) {
			case X11.SelectionRequest:
				// someone wants data to be sent to them
				XSelectionRequestEvent sre = (XSelectionRequestEvent) event
						.getTypedValue(X11.XSelectionRequestEvent.class);
				sre.autoRead();

				XSelectionEvent sne = new XSelectionEvent();
				sne.display = display;
				sne.type = X11.SelectionNotify;
				sne.requestor = sre.requestor;
				sne.selection = sre.selection;
				sne.time = sre.time;
				sne.target = sre.target;
				sne.property = sre.property;
				sne.autoWrite();
				logger.debug("selection requested " + sre.target + "=" + x11.XGetAtomName(display, sre.target));
				XEvent eventToSend = new XEvent();
				eventToSend.setTypedValue(sne);
				Memory mem = new Memory(1 * (data.getBytes().length + 1));
				mem.clear();
				mem.setString(0, data);
				x11.XChangeProperty(display, sre.requestor, sre.property, X11.XA_STRING, 8, X11.PropModeReplace, mem,
						data.length());
				x11.XSendEvent(display, sre.requestor, X11.NoEventMask, new NativeLong(0), eventToSend);
				break;
			case X11.SelectionClear:
				// someone took selection from me
				logger.debug("selection taken");
				break;
			default:
				// ignore
			}
		}
	}

	private ClipboardData getClipboardDataInternal(String format) {
		WindowProperty prop = getClipboardDataRaw(format);
		if (prop == null) {
			// failed
			// TODO
			throw new RuntimeException("TODO HANDLE FAILURE");
		} else {
			// succeeded
			if (prop.format == 0) {
				return new EmptyClipboardData(ClipboardFormat.fromLinux(format));
			} else {
				// logger.debug(propReturn.getValue().getString(0));
				return convertClipboardData(format, prop.property);
			}
		}
	}

	/**
	 * returns null on error
	 *
	 * @param format
	 */
	private WindowProperty getClipboardDataRaw(String format) {
		// TODO implement increment protocol
		// Atom increment = x11.XInternAtom(display, "INCR", false);
		// default selection is CLIPBOARD

		// format="TARGETS"
		convertSelectionAndSync(format);
		XEvent event = new XEvent();
		do {
			int ret = x11.XNextEvent(display, event);
			event.autoRead();
			if (logger.isTraceEnabled()) {
				logger.trace("got event: " + event);
			}
		} while (event.type != X11.SelectionNotify);
		// larger than should be, but this allows us to ignore increment protocol
		// longer.
		long propSize = 1024 * 1024 * 2;
		boolean delete = false;
		AtomByReference actualTypeReturn = new AtomByReference();
		IntByReference actualFormatReturn = new IntByReference();
		NativeLongByReference nItemsReturn = new NativeLongByReference();
		NativeLongByReference bytesAfterReturn = new NativeLongByReference();
		PointerByReference propReturn = new PointerByReference();
		Atom anyPropAtom = new Atom(X11.AnyPropertyType);
		int ret = x11.XGetWindowProperty(display, window, selectionDataProperty, new NativeLong(X11.CurrentTime),
				new NativeLong(propSize), delete, anyPropAtom, actualTypeReturn, actualFormatReturn, nItemsReturn,
				bytesAfterReturn, propReturn);
		if (logger.isDebugEnabled()) {
			logger.debug("Type: " + actualTypeReturn.getValue() + " Format: " + actualFormatReturn.getValue());
			logger.debug("nItems: " + nItemsReturn.getValue() + " bytesAfter: " + bytesAfterReturn.getValue());
		}
		long bytesLeft = bytesAfterReturn.getValue().longValue();
		if (bytesLeft > 0) {
			logger.error("Clipboard data too large.  Only returning partial with " + bytesLeft
					+ " bytes remaining.  Need to implement increment protocol!");
		}
		if (ret == X11.Success) {
			WindowProperty prop = new WindowProperty(propReturn.getValue(), actualTypeReturn.getValue(),
					actualFormatReturn.getValue(), nItemsReturn.getValue().longValue());
			return prop;
		} else {
			return null;
		}
	}

	// see
	// https://stackoverflow.com/questions/27378318/c-get-string-from-clipboard-on-linux/44992938#44992938
	private void printSelection(String format, boolean checkForEventsInFunction) {
		Atom increment = x11.XInternAtom(display, "INCR", false);
		// default selection is CLIPBOARD
		convertSelectionAndSync(format);
		System.out.println("requested format");
		if (checkForEventsInFunction) {
			XEvent event = null;
			do {
				event = getNextEventWithTimeout(3000, 100);
				event.autoRead();
				System.out.println("event type=" + event.type);
				if (event.type == X11.PropertyNotify) {
					XPropertyEvent propEvent = (XPropertyEvent) event.getTypedValue(XPropertyEvent.class);
					propEvent.autoRead();
					System.out.println(propEvent + " " + propEvent.atom);
				}
			} while (event.type != X11.SelectionNotify);
		} else {
			while (!selectionAvailable) {
				JavaUtil.sleepAndLogInterruption(100);
			}
		}
		selectionAvailable = false;
		// System.out.println("event type=" + event.type);
		// XSelectionEvent selEvent = (XSelectionEvent)
		// event.getTypedValue(XSelectionEvent.class);
		// selEvent.autoRead();
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
				new NativeLong(propSize), delete, anyPropAtom, actualTypeReturn, actualFormatReturn, nItemsReturn,
				bytesAfterReturn, propReturn);
		if (ret != X11.Success) {
			System.out.println("UNSUCCESSFUL");
		} else {
			System.out.println("ret=" + ret);
			System.out.println("Type: " + actualTypeReturn.getValue());
			System.out.println("Format: " + actualFormatReturn.getValue() + "  Incr: " + increment);
			System.out.println("nItems: " + nItemsReturn.getValue());
			System.out.println("bytesAfter: " + bytesAfterReturn.getValue());
			// System.out.println(propReturn.getValue());

			if (actualTypeReturn.getValue() != null) {
				String data = propReturn.getValue().getString(0);
				int maxLengthToPrint = 64;
				System.out.println("String: " + data.length() + " - "
						+ (data.length() > maxLengthToPrint ? data.substring(0, maxLengthToPrint) : data));
			} else {
				System.out.println("No Data!");
			}
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

	private ClipboardData convertClipboardData(String format, Pointer dataPointer) {
		ClipboardFormat cf = ClipboardFormat.fromLinux(format);
		switch (cf) {
		case TEXT:
			String str = dataPointer.getString(0);
			return new PlainTextClipboardData(str);
		case WIDE_TEXT:
			str = dataPointer.getWideString(0);
			return new WideTextClipboardData(str);
		default:
			return new UnknownClipboardData(cf);
		}
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
		// x11.XConvertSelection(display, clipboard, formatAtom, Atom.None, window, new
		// NativeLong(0));
		x11.XSync(display, false);
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

	private static class WindowProperty {

		public Pointer property;
		public Atom type;
		public int format;
		public long numItems;

		public WindowProperty(Pointer property, Atom type, int format, long numItems) {
			this.property = property;
			this.type = type;
			this.format = format;
			this.numItems = numItems;
		}

	}

}
