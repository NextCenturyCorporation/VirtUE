package com.ncc.savior.desktop.clipboard.linux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	private Atom targets;

	private IClipboardListener listener;

	private XSelectionEvent SelectionNotifyEventToSend;
	private volatile WindowProperty dataWindowProp;
	private volatile WindowProperty formatWindowProp;

	private Runnable listenerThread;

	protected ScheduledExecutorService executor;

	private Collection<ClipboardFormat> delayedFormats;
	protected ConcurrentLinkedQueue<Runnable> runnableQueue;

	public X11ClipboardWrapper() {
		delayedFormats = new TreeSet<ClipboardFormat>();
		runnableQueue = new ConcurrentLinkedQueue<Runnable>();
		x11 = ILinuxClipboardX11.INSTANCE;
		listenerThread = new Runnable() {

			@Override
			public void run() {
				display = X11.INSTANCE.XOpenDisplay(null);
				clipboardAtom = x11.XInternAtom(display, "CLIPBOARD", false);
				selectionDataProperty = x11.XInternAtom(display, "XSEL_DATA", false);
				targets = x11.XInternAtom(display, "TARGETS", false);
				int screen = x11.XDefaultScreen(display);
				Window defaultWindow = X11.INSTANCE.XRootWindow(display, screen);
				window = x11.XCreateSimpleWindow(display, defaultWindow, 0, 0, 1, 1, 0, 1, 1);
				System.out.println("MyWindow: " + window);

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
				delayedFormats.add(ClipboardFormat.TEXT);
				delayedFormats.add(ClipboardFormat.WIDE_TEXT);
				// clear?
				getWindowProperty();
				becomeSelectionOwner();
				logger.debug("x11 has taken clipboard");

				XEvent event = new XEvent();
				while (true) {
					int queued = x11.XEventsQueued(display, 2);
					logger.trace("Events Queued: " + queued);
					if (queued > 0 || false) {
						x11.XNextEvent(display, event);
						logger.debug("processing event " + event.type);
						switch (event.type) {
						case X11.SelectionRequest:
							// a local application has tried to paste data and is requesting the data be
							// sent to them
							XSelectionRequestEvent incomingSre = (XSelectionRequestEvent) event
									.getTypedValue(X11.XSelectionRequestEvent.class);
							incomingSre.autoRead();
							logger.debug("read sre");
							if (incomingSre.target.equals(targets)) {
								logger.debug("SelectionRequestEvent Targets: " + incomingSre);
							} else {
								logger.debug("SelectionRequestEvent Data: " + incomingSre);
							}

							XSelectionEvent outgoingSne = new XSelectionEvent();
							outgoingSne.display = display;
							outgoingSne.type = X11.SelectionNotify;
							outgoingSne.requestor = incomingSre.requestor;
							outgoingSne.selection = incomingSre.selection;
							outgoingSne.time = incomingSre.time;
							outgoingSne.target = incomingSre.target;
							outgoingSne.property = incomingSre.property;
							outgoingSne.autoWrite();
							logger.debug("SRE target=" + incomingSre.target + "  targets from class=" + targets);
							if (incomingSre.target.equals(targets)) {
								// local application is asking for formats
								logger.debug("Requested targets");
								sendFormats(outgoingSne);
							} else {
								logger.debug("selection requested " + incomingSre.target + "="
										+ x11.XGetAtomName(display, incomingSre.target));
								// Tell the hub that a paste attempt happened. It will send data and that data
								// will be written in another thread.
								SelectionNotifyEventToSend = outgoingSne;
								listener.onPasteAttempt(
										ClipboardFormat.fromLinux(x11.XGetAtomName(display, outgoingSne.target)));
							}

							break;
						case X11.SelectionNotify:
							// lets us know that another client has a selection ready for us. This selection
							// could be formats or data.
							XSelectionEvent incomingSne = (XSelectionEvent) event
									.getTypedValue(X11.XSelectionEvent.class);
							incomingSne.autoRead();
							if (incomingSne.target.equals(targets)) {
								logger.debug("Got Event: " + "selection Notify targets");
								formatWindowProp = getWindowProperty();

							} else {
								logger.debug("Got Event: " + "selection Notify data");
								dataWindowProp = getWindowProperty();
							}

							break;

						case X11.SelectionClear:
							logger.debug("Got Event: " + "selection clear");
							// someone took selection from me
							convertSelectionAndSync(targets);
							executor.schedule(() -> {
								// TODO this could possibly drop events!
								Set<String> af = getAvailableFormatsInMyThread();
								TreeSet<ClipboardFormat> acf;
								acf = new TreeSet<ClipboardFormat>();
								for (String f : af) {
									ClipboardFormat fmt = ClipboardFormat.fromLinux(f);
									// logger.debug(f + " -> " + (fmt == null ? null : fmt.getLinux()));
									if (fmt != null) {
										acf.add(fmt);
									}
								}
								listener.onClipboardChanged(acf);
								logger.debug("selection taken");
							}, 1, TimeUnit.MICROSECONDS);
							break;
						default:
							// ignore
						}
					} else {
						Runnable r = runnableQueue.poll();
						if (r != null) {
							r.run();
						} else {
							// No events and No runnables
							JavaUtil.sleepAndLogInterruption(10);
						}
					}
				}
			}
		};

		ThreadFactory threadFactory = new ThreadFactory() {
			private int i = 1;

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, getName());
			}

			private synchronized String getName() {
				// increments after returning value;
				return "x11-clipboard-" + i++;
			}
		};

		this.executor = Executors.newScheduledThreadPool(3, threadFactory);
		this.executor.schedule(listenerThread, 300, TimeUnit.MILLISECONDS);
		// listenerThread.run();
	}

	@Override
	public void setDelayedRenderFormats(Collection<ClipboardFormat> formats) {
		delayedFormats.clear();
		delayedFormats.addAll(formats);
		becomeSelectionOwner();
		logger.debug("setting formats and took clipboard: " + formats);
	}

	@Override
	public void setClipboardListener(IClipboardListener listener) {
		this.listener = listener;
		// listenerThread.run();
	}

	@Override
	public void setDelayedRenderData(ClipboardData clipboardData) {
		PointerByReference pbr = new PointerByReference();
		int lengthInBytes = clipboardData.getLinuxData(pbr);
		Pointer ptr = pbr.getValue();
		// This is caused by an event from in linux and we need that event.
		XSelectionEvent sne = this.SelectionNotifyEventToSend;
		Atom atom = x11.XInternAtom(display, clipboardData.getFormat().getLinux(), false);
		sendSelectionNotifyEvent(lengthInBytes, ptr, sne, 8, atom);
	}

	@Override
	public ClipboardData getClipboardData(ClipboardFormat format) {
		Atom formatAtom = x11.XInternAtom(display, format.getLinux(), false);
		runnableQueue.offer(() -> {
			convertSelectionAndSync(formatAtom);
			logger.debug("called convert selection for data " + format);
		});
		WindowProperty myprop = waitForUpdatedDataWindowProp();
		if (myprop == null) {
			myprop = getWindowProperty();
		}
		ClipboardData data = convertClipboardData(format, myprop);
		return data;
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
		IClipboardListener myListener = new IClipboardListener() {

			@Override
			public void onPasteAttempt(ClipboardFormat format) {
				logger.debug("paste attempt: " + format);
				wrapper.setDelayedRenderData(new PlainTextClipboardData("it works?"));

			}

			@Override
			public void onClipboardChanged(Set<ClipboardFormat> formats) {
				logger.debug("clipboard changed hardcoded callback: " + formats);
				Runnable r = new Runnable() {

					@Override
					public void run() {
						ClipboardData data = wrapper.getClipboardData(formats.iterator().next());
						logger.debug("data: " + data);
					}

				};
				new Thread(r).start();
			}
		};
		wrapper.setClipboardListener(myListener);
		String format1 = "UTF8_STRING";// OR UTF8_STRING
		String format2 = "STRING";

		JavaUtil.sleepAndLogInterruption(500);
		// WindowProperty prop = wrapper.getClipboardDataRaw(X11.XA_STRING);
		// logger.debug("format1: " + prop.property.getString(0));

		// Set<String> formats = wrapper.getAvailableFormats();
		// System.out.println(formats);
		boolean cont = false;
		while (cont) {
			// wrapper.setClipboardDataString("test");
			JavaUtil.sleepAndLogInterruption(5000);

			// ClipboardData data = wrapper.getClipboardDataInternal(format2);
			// wrapper.printSelection(format2, true);
			// logger.debug(data.toString());
			JavaUtil.sleepAndLogInterruption(5000);
		}

	}

	private void sendSelectionNotifyEvent(int numItems, Pointer ptr, XSelectionEvent sne, int formatOrSizeInBits,
			Atom type) {
		logger.debug("sending SelectionNotifyEvent: " + type + " " + x11.XGetAtomName(display, type));
		x11.XChangeProperty(display, sne.requestor, sne.property, type, formatOrSizeInBits, X11.PropModeReplace, ptr,
				numItems);
		XEvent eventToSend = new XEvent();
		eventToSend.setTypedValue(sne);
		x11.XSendEvent(display, sne.requestor, X11.NoEventMask, new NativeLong(0), eventToSend);
	}

	private void sendFormats(XSelectionEvent sne) {
		ArrayList<ClipboardFormat> formats = new ArrayList<ClipboardFormat>(delayedFormats);
		int itemSize = 8;
		int lengthInBytes = itemSize * formats.size();
		Memory memory = new Memory(lengthInBytes);
		memory.clear();
		logger.debug("itemSize: " + itemSize);
		for (int i = 0; i < formats.size(); i++) {
			Atom atom = x11.XInternAtom(display, formats.get(i).getLinux(), false);
			logger.debug("writting atom to " + i + " " + atom.intValue() + "  " + atom);
			memory.setNativeLong(i * itemSize, new NativeLong(atom.longValue()));
			// memory.setLong(i * NativeLong.SIZE, new NativeLong(atom.longValue()));
		}
		sendSelectionNotifyEvent(formats.size(), memory, sne, 32, X11.XA_ATOM);
	}

	private Set<String> getAvailableFormatsInMyThread() {
		logger.debug("requesting selection: formats");
		Set<String> formats = new LinkedHashSet<String>();
		WindowProperty raw = waitForUpdatedFormatWindowProp();
		logger.debug("Waited for formats format: " + raw.formatBytes + " type: " + raw.type + " "
				+ x11.XGetAtomName(display, raw.type));
		for (int i = 0; i < raw.numItems; i++) {
			NativeLong nl = raw.property.getNativeLong(i * NativeLong.SIZE);
			// logger.debug(" " + val + " 0x" + Long.toHexString(val));
			// logger.debug(" " + nl + " : " + nl);
			Atom formatAtom = new Atom(nl.longValue());
			// String formatName = x11.XGetAtomName(display, formatAtom);
			logger.debug("FIXME: forced format to string");
			String formatName = "STRING";
			formats.add(formatName);
		}
		logger.debug(formats.toString());
		return formats;
	}

	private Set<String> getAvailableFormats() {
		// is order important? I'm not sure at this point.

		logger.debug("requesting selection");
		convertSelectionAndSync(targets);
		return waitForAvailableFormats();
	}

	private Set<String> waitForAvailableFormats() {
		Set<String> formats = new LinkedHashSet<String>();
		WindowProperty raw = waitForUpdatedFormatWindowProp();
		logger.debug("Waited for formats format: " + raw.formatBytes + " type: " + raw.type + " "
				+ x11.XGetAtomName(display, raw.type));
		// This might only work for type=XA_ATOM. I saw one source saying the TARGETS is
		// sometimes an array of ATOMs and sometimes something else.
		for (int i = 0; i < raw.numItems; i++) {
			NativeLong nl = raw.property.getNativeLong(i * NativeLong.SIZE);
			// logger.debug(" " + val + " 0x" + Long.toHexString(val));
			// logger.debug(" " + nl + " : " + nl);
			Atom formatAtom = new Atom(nl.longValue());
			// String formatName = x11.XGetAtomName(display, formatAtom);
			String formatName = "STRING";
			formats.add(formatName);
		}
		logger.debug(formats.toString());
		return formats;
	}

	private synchronized WindowProperty waitForUpdatedFormatWindowProp() {

		while (formatWindowProp == null) {
			logger.debug("Waiting for format prop... " + formatWindowProp);
			JavaUtil.sleepAndLogInterruption(100);
		}
		logger.debug("format waiting done");
		WindowProperty raw = formatWindowProp;
		formatWindowProp = null;
		return raw;
	}

	private synchronized WindowProperty waitForUpdatedDataWindowProp() {
		long stopTime = System.currentTimeMillis() + 2000;
		while (dataWindowProp == null) {
			logger.debug("Waiting for data prop... " + dataWindowProp);
			JavaUtil.sleepAndLogInterruption(100);
			if (stopTime < System.currentTimeMillis()) {
				logger.warn("waiting has timed out!");
				return null;
			}
		}
		logger.debug("data waiting done");
		WindowProperty raw = dataWindowProp;
		dataWindowProp = null;
		return raw;
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
	}

	private ClipboardData getClipboardDataInternal(String format) {
		Atom formatAtom = x11.XInternAtom(display, format, false);
		WindowProperty prop = getClipboardDataRaw(formatAtom);
		if (prop == null) {
			// failed
			// TODO
			throw new RuntimeException("TODO HANDLE FAILURE");
		} else {
			// succeeded
			if (prop.formatBytes == 0) {
				return new EmptyClipboardData(ClipboardFormat.fromLinux(format));
			} else {
				// logger.debug(propReturn.getValue().getString(0));
				ClipboardFormat cf = ClipboardFormat.fromLinux(format);
				return convertClipboardData(cf, prop);
			}
		}
	}

	/**
	 * returns null on error
	 *
	 * @param format
	 */
	private WindowProperty getClipboardDataRaw(Atom format) {
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
		return getWindowProperty();
	}

	private WindowProperty getWindowProperty() {
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
		Atom formatAtom = x11.XInternAtom(display, format, false);
		convertSelectionAndSync(formatAtom);
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

	private ClipboardData convertClipboardData(ClipboardFormat cf, WindowProperty myprop) {
		Pointer property = myprop.property;
		switch (cf) {
		case TEXT:
			String str = property.getString(0);
			return new PlainTextClipboardData(str);
		case WIDE_TEXT:
			str = property.getWideString(0);
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

	private void convertSelectionAndSync(Atom formatAtom) {
		String selection = "CLIPBOARD";
		Atom clipboard = x11.XInternAtom(display, selection, false);
		x11.XConvertSelection(display, clipboard, formatAtom, selectionDataProperty, window, new NativeLong(0));
		// x11.XConvertSelection(display, clipboard, formatAtom, Atom.None, window, new
		// NativeLong(0));
		// x11.XSync(display, false);
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
		public int formatBytes;
		public long numItems;

		public WindowProperty(Pointer property, Atom type, int format, long numItems) {
			this.property = property;
			this.type = type;
			this.formatBytes = format;
			this.numItems = numItems;
		}

	}

}
