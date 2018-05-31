package com.ncc.savior.desktop.clipboard.linux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnknownClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnicodeClipboardData;
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
import com.sun.jna.platform.unix.X11.XSelectionEvent;
import com.sun.jna.platform.unix.X11.XSelectionRequestEvent;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

public class X11ClipboardWrapper implements IClipboardWrapper {
	private static final Logger logger = LoggerFactory.getLogger(X11ClipboardWrapper.class);

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

	private Runnable mainClipboardRunnable;

	private Collection<ClipboardFormat> delayedFormats;
	protected ConcurrentLinkedQueue<Runnable> runnableQueue;

	private Thread mainClipboardThread;

	public X11ClipboardWrapper() {
		delayedFormats = new TreeSet<ClipboardFormat>();
		runnableQueue = new ConcurrentLinkedQueue<Runnable>();
		x11 = ILinuxClipboardX11.INSTANCE;
		mainClipboardRunnable = new Runnable() {

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
				delayedFormats.add(ClipboardFormat.UNICODE);
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
								String formatName = x11.XGetAtomName(display, outgoingSne.target);
								// i wish switch statements had their own variable scope...
								ClipboardFormat myCf = ClipboardFormat.fromLinux(formatName);
								logger.debug("send paste event message format=" + myCf + " formatName=" + formatName);
								if (myCf == null) {
									// TODO in linux, you sometimes get weird formats even though you said you don't
									// support them. We should fix this later.
									logger.warn("requested invalid format=" + formatName);
									// setDelayedRenderData(new EmptyClipboardData(myCf));
									myCf = ClipboardFormat.TEXT;
								}
								// effectively final so we can use in other threads.
								ClipboardFormat finalCf = myCf;
								Runnable runLater = () -> {
									listener.onPasteAttempt(finalCf);
								};
								new Thread(runLater).start();

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
							Runnable runLater = () -> {
								Set<String> af = waitForAvailableFormats();
								TreeSet<ClipboardFormat> acf;
								acf = new TreeSet<ClipboardFormat>();
								for (String f : af) {
									ClipboardFormat fmt = ClipboardFormat.fromLinux(f);
									// logger.debug(f + " -> " + (fmt == null ? null : fmt.getLinux()));
									if (fmt != null) {
										acf.add(fmt);
									}
								}
								logger.debug("sending clipboard Changed message.  Formats=" + acf);
								listener.onClipboardChanged(acf);
								logger.debug("selection taken");
							};
							// should we use something else to handle threads?
							new Thread(runLater).start();
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

		mainClipboardThread = new Thread(mainClipboardRunnable, "X11-clipboard-main");
		// mainClipboardThread.setDaemon(true);
		mainClipboardThread.start();
	}

	@Override
	public void setDelayedRenderFormats(Collection<ClipboardFormat> formats) {
		runnableQueue.offer(() -> {
			delayedFormats.clear();
			delayedFormats.addAll(formats);
			becomeSelectionOwner();
			logger.debug("setting formats and took clipboard: " + formats);
		});
	}

	@Override
	public void setClipboardListener(IClipboardListener listener) {
		this.listener = listener;
		// listenerThread.run();
	}

	@Override
	public void setDelayedRenderData(ClipboardData clipboardData) {
		runnableQueue.offer(() -> {
			PointerByReference pbr = new PointerByReference();

			Pointer ptr = clipboardData.getLinuxData();
			int numItems = clipboardData.getLinuxNumEntries();
			int itemSize = clipboardData.getLinuxEntrySizeBits();
			// This is caused by an event from in linux and we need that event.
			XSelectionEvent sne = this.SelectionNotifyEventToSend;
			Atom atom = x11.XInternAtom(display, clipboardData.getFormat().getLinux(), false);
			logger.debug("sending notify event: numitems: " + numItems + " itemSize=" + itemSize);
			sendSelectionNotifyEvent(numItems, ptr, sne, itemSize, atom);
		});
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
		runnableQueue.offer(() -> {
			x11.XSetSelectionOwner(display, clipboardAtom, window, new NativeLong(X11.CurrentTime));
		});
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

	private Set<String> waitForAvailableFormats() {
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
			String formatName = x11.XGetAtomName(display, formatAtom);
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

	private ClipboardData convertClipboardData(ClipboardFormat cf, WindowProperty myprop) {
		Pointer property = myprop.property;
		switch (cf) {
		case TEXT:
			String str = property.getString(0);
			return new PlainTextClipboardData(str);
		case UNICODE:
			str = property.getString(0, "UTF8");
			return new UnicodeClipboardData(str);
		default:
			return new UnknownClipboardData(cf);
		}
	}

	/**
	 * Should always be run in main x11 thread!
	 *
	 * @param formatAtom
	 */
	private void convertSelectionAndSync(Atom formatAtom) {
		String selection = "CLIPBOARD";
		Atom clipboard = x11.XInternAtom(display, selection, false);
		x11.XConvertSelection(display, clipboard, formatAtom, selectionDataProperty, window, new NativeLong(0));
		// x11.XConvertSelection(display, clipboard, formatAtom, Atom.None, window, new
		// NativeLong(0));
		// x11.XSync(display, false);
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
