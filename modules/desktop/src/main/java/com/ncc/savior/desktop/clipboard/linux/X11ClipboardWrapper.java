package com.ncc.savior.desktop.clipboard.linux;

import java.util.Collection;
import java.util.Date;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.util.JavaUtil;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Atom;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.unix.X11.XErrorEvent;
import com.sun.jna.platform.unix.X11.XErrorHandler;
import com.sun.jna.platform.unix.X11.XEvent;
import com.sun.jna.platform.unix.X11.XSelectionClearEvent;
import com.sun.jna.platform.unix.X11.XSelectionEvent;
import com.sun.jna.platform.unix.X11.XSelectionRequestEvent;

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
