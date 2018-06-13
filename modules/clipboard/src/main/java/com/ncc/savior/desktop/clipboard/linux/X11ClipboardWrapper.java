package com.ncc.savior.desktop.clipboard.linux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnicodeClipboardData;
import com.ncc.savior.desktop.clipboard.data.UnknownClipboardData;
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

/**
 * Wrapper class for X11 that wraps the clipboard into a generic
 * {@link IClipboardWrapper}.
 *
 * In X11, clipboards are called selection and there are any number of
 * selections. There are 3 defined selections, but we are only concerned with 1
 * which is called 'clipboard' and used for the standard copy/paste mechanic.
 * Formats in x11 are often called targets. To get the targets/formats,
 * applications attempt to paste the 'clipboard' selection in the 'TARGETS'
 * format. This data will be a list of formats.
 *
 * X11 clipboard differs from the windows clipboard in that there is no callback
 * for when the clipboard itself has changed. This will need to be accounted for
 * in a higher level class as requests for clipboard data when an X11 clipboard
 * is the owner will ALWAYS need to go all the way back to the owner. In
 * windows, you are notified whenever the clipboard is changed so you can do
 * some caching for efficiency.
 *
 */
public class X11ClipboardWrapper implements IClipboardWrapper {
	private static final Logger logger = LoggerFactory.getLogger(X11ClipboardWrapper.class);

	private ILinuxClipboardX11 x11;
	private Display display;
	private Window window;

	// we store some commonly used atoms for easy reuse
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

	private TreeSet<ClipboardFormat> previousFormats;

	private volatile boolean ownSelection;

	protected boolean stopMainClipboardThread = false;

	private boolean stopTargetPollThread = false;

	private Thread targetPollThread;

	public X11ClipboardWrapper(boolean takeClipboard) {
		delayedFormats = new TreeSet<ClipboardFormat>();
		runnableQueue = new ConcurrentLinkedQueue<Runnable>();
		x11 = ILinuxClipboardX11.INSTANCE;

		mainClipboardRunnable = new Runnable() {

			@Override
			public void run() {
				// x11.XInitThreads();
				display = X11.INSTANCE.XOpenDisplay(null);
				clipboardAtom = x11.XInternAtom(display, "CLIPBOARD", false);
				selectionDataProperty = x11.XInternAtom(display, "XSEL_DATA", false);
				targets = x11.XInternAtom(display, "TARGETS", false);
				int screen = x11.XDefaultScreen(display);
				Window defaultWindow = X11.INSTANCE.XRootWindow(display, screen);
				window = x11.XCreateSimpleWindow(display, defaultWindow, 0, 0, 1, 1, 0, 1, 1);
				logger.debug("MyWindow: " + window);

				NativeLong eventMask = new NativeLong(X11.PropertyChangeMask);
				x11.XSelectInput(display, window, eventMask);
				XErrorHandler handler = new XErrorHandler() {

					@Override
					public int apply(Display display, XErrorEvent errorEvent) {
						byte[] buffer = new byte[2048];
						x11.XGetErrorText(display, errorEvent.error_code, buffer, 2048);
						logger.error("ERROR: " + new String(buffer));
						return 1;
					}
				};
				startTargetPollThread();

				x11.XSetErrorHandler(handler);
				delayedFormats.add(ClipboardFormat.TEXT);
				delayedFormats.add(ClipboardFormat.UNICODE);
				// clear?
				getWindowProperty();
				if (takeClipboard) {
					becomeSelectionOwner();
					logger.debug("x11 has taken clipboard");
				}

				XEvent event = new XEvent();
				while (!stopMainClipboardThread) {
					int queued = x11.XEventsQueued(display, 2);
					logger.trace("Events Queued: " + queued);
					if (queued > 0 || false) {
						x11.XNextEvent(display, event);
						switch (event.type) {
						case X11.SelectionRequest:
							// a local application has tried to paste data and is requesting the data be
							// sent to them
							XSelectionRequestEvent incomingSre = (XSelectionRequestEvent) event
									.getTypedValue(X11.XSelectionRequestEvent.class);
							incomingSre.autoRead();
							if (incomingSre.target.equals(targets)) {
								logger.debug("SelectionRequestEvent Targets: ");
							} else {
								logger.debug("SelectionRequestEvent Data  target: "
										+ x11.XGetAtomName(display, incomingSre.target));
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
							if (incomingSre.target.equals(targets)) {
								// local application is asking for formats
								sendFormats(outgoingSne);
							} else {
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
								logger.trace("Got Event: selection Notify targets");
								formatWindowProp = getWindowProperty();

							} else {
								logger.trace("Got Event: selection Notify data");
								dataWindowProp = getWindowProperty();
							}

							break;

						case X11.SelectionClear:
							logger.trace("Got Event: selection clear");
							ownSelection = false;
							// someone took selection from me
							// handling the loss of selection will be handled in the target poll thread.
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
				x11.XDestroyWindow(display, window);
				listener.closed();
			}
		};

		mainClipboardThread = new Thread(mainClipboardRunnable, "X11-clipboard-main");
		mainClipboardThread.setDaemon(true);
		mainClipboardThread.start();

	}

	private void startTargetPollThread() {
		Runnable targetPollRunnable = () -> {
			previousFormats = new TreeSet<ClipboardFormat>();
			while (!stopTargetPollThread) {
				if (!ownSelection) {
					runnableQueue.offer(() -> {
						if (logger.isTraceEnabled()) {
							logger.trace("calling convert selection for targets");
						}
						convertSelectionAndSync(targets);
					});
					Set<String> af = waitForAvailableFormats();
					if (logger.isTraceEnabled()) {
						logger.trace("received available targets");
					}
					TreeSet<ClipboardFormat> acf;
					acf = new TreeSet<ClipboardFormat>();
					for (String f : af) {
						ClipboardFormat fmt = ClipboardFormat.fromLinux(f);
						// logger.debug(f + " -> " + (fmt == null ? null : fmt.getLinux()));
						if (fmt != null) {
							acf.add(fmt);
						}
					}
					boolean equals = (acf.isEmpty() && previousFormats.isEmpty()) || acf.equals(previousFormats);
					if (!equals) {
						if (logger.isTraceEnabled()) {
							logger.trace("sending clipboard Changed message.  Formats=" + acf);
						}
						if (!ownSelection) {
							previousFormats = acf;
							listener.onClipboardChanged(acf);
						}
					}

				} else {
					previousFormats = new TreeSet<ClipboardFormat>();
				}
				// TODO smarter period control?
				JavaUtil.sleepAndLogInterruption(100);
			}
		};
		targetPollThread = new Thread(targetPollRunnable, "TargetsPoller");
		targetPollThread.setDaemon(true);
		targetPollThread.start();
	}

	@Override
	public void setDelayedRenderFormats(Set<ClipboardFormat> formats) {
		// X11 is effectively always in delayed render mode when the application owns
		// the clipboard
		runnableQueue.offer(() -> {
			delayedFormats.clear();
			delayedFormats.addAll(formats);
			becomeSelectionOwner();
		});
	}

	@Override
	public void setClipboardListener(IClipboardListener listener) {
		this.listener = listener;
	}

	@Override
	public void setDelayedRenderData(ClipboardData clipboardData) {
		runnableQueue.offer(() -> {
			Pointer ptr = clipboardData.createLinuxData();
			int numItems = clipboardData.returnLinuxNumEntries();
			int itemSize = clipboardData.returnLinuxEntrySizeBits();
			// This is caused by an event from in linux. We stored an event to send based on
			// the event received and we need that event.
			XSelectionEvent sne = this.SelectionNotifyEventToSend;
			Atom atom = x11.XInternAtom(display, clipboardData.getFormat().getLinux(), false);
			if (logger.isTraceEnabled()) {
				logger.trace("sending notify event: numitems: " + numItems + " itemSize=" + itemSize);
			}
			sendSelectionNotifyEvent(numItems, ptr, sne, itemSize, atom);
		});
	}

	@Override
	public ClipboardData getClipboardData(ClipboardFormat format) {
		Atom formatAtom = x11.XInternAtom(display, format.getLinux(), false);
		runnableQueue.offer(() -> {
			convertSelectionAndSync(formatAtom);
			if (logger.isTraceEnabled()) {
				logger.trace("called convert selection for data " + format);
			}
		});
		WindowProperty myprop = waitForUpdatedDataWindowProp();
		if (myprop == null) {
			// somewhat to clear the thread if the waiting timed out. this shouldn't happen.
			myprop = getWindowProperty();
		}
		ClipboardData data = convertClipboardData(format, myprop);
		return data;
	}

	public void becomeSelectionOwner() {
		ownSelection = true;
		runnableQueue.offer(() -> {
			x11.XSetSelectionOwner(display, clipboardAtom, window, new NativeLong(X11.CurrentTime));
		});
	}

	@Override
	public void close() throws IOException {

		try {
			stopTargetPollThread = true;
			targetPollThread.join();
		} catch (InterruptedException e) {
			logger.warn("Waiting for target poll thread to stop interrupted", e);
		}
		stopMainClipboardThread = true;

	}

	/**
	 * MUST be called from main loop
	 */
	private void sendSelectionNotifyEvent(int numItems, Pointer ptr, XSelectionEvent sne, int formatOrSizeInBits,
			Atom type) {
		logger.debug("sending SelectionNotifyEvent: " + type + " " + x11.XGetAtomName(display, type));
		x11.XChangeProperty(display, sne.requestor, sne.property, type, formatOrSizeInBits, X11.PropModeReplace, ptr,
				numItems);
		XEvent eventToSend = new XEvent();
		eventToSend.setTypedValue(sne);
		x11.XSendEvent(display, sne.requestor, X11.NoEventMask, new NativeLong(0), eventToSend);
	}

	// called from event loop
	private void sendFormats(XSelectionEvent sne) {
		if (delayedFormats.size() > 0) {
			ArrayList<ClipboardFormat> formats = new ArrayList<ClipboardFormat>(delayedFormats);
			int itemSize = 8;
			int lengthInBytes = itemSize * formats.size();
			Memory memory = new Memory(lengthInBytes);
			memory.clear();
			if (logger.isTraceEnabled()) {
				logger.trace("itemSize: " + itemSize);
			}
			// For each format, get the Atom for it and put that in memory (as an array).
			for (int i = 0; i < formats.size(); i++) {
				Atom atom = x11.XInternAtom(display, formats.get(i).getLinux(), false);
				if (logger.isTraceEnabled()) {
					logger.trace("writting atom to " + i + " " + atom.intValue() + "  " + atom);
				}
				// write Atom to memory.
				memory.setNativeLong(i * itemSize, new NativeLong(atom.longValue()));
			}
			// 32 bit magic number because thats the size of Atoms
			sendSelectionNotifyEvent(formats.size(), memory, sne, 32, X11.XA_ATOM);
		}
	}

	private Set<String> waitForAvailableFormats() {
		logger.trace("requesting selection: formats");
		Set<String> formats = new LinkedHashSet<String>();
		WindowProperty raw = waitForUpdatedFormatWindowProp();
		if (logger.isTraceEnabled()) {
			logger.debug("Waited for formats format: " + raw.formatBytes + " type: " + raw.type + " "
					+ x11.XGetAtomName(display, raw.type));
		}
		// TODO what if raw is null because it timed out (initialization and no
		// clipboard)
		for (int i = 0; i < raw.numItems; i++) {
			NativeLong nl = raw.property.getNativeLong(i * NativeLong.SIZE);
			// logger.debug(" " + val + " 0x" + Long.toHexString(val));
			// logger.debug(" " + nl + " : " + nl);
			Atom formatAtom = new Atom(nl.longValue());
			String formatName = x11.XGetAtomName(display, formatAtom);
			formats.add(formatName);
		}
		return formats;
	}

	private synchronized WindowProperty waitForUpdatedFormatWindowProp() {
		long stopTime = System.currentTimeMillis() + 2000;
		while (formatWindowProp == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Waiting for format prop... " + formatWindowProp);
			}
			JavaUtil.sleepAndLogInterruption(100);
			if (stopTime < System.currentTimeMillis()) {
				logger.warn("waiting has timed out!");
				return null;
			}
		}
		logger.trace("format waiting done");
		WindowProperty raw = formatWindowProp;
		formatWindowProp = null;
		return raw;
	}

	private synchronized WindowProperty waitForUpdatedDataWindowProp() {
		long stopTime = System.currentTimeMillis() + 2000;
		while (dataWindowProp == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Waiting for data prop... " + dataWindowProp);
			}
			JavaUtil.sleepAndLogInterruption(100);
			if (stopTime < System.currentTimeMillis()) {
				logger.warn("waiting has timed out!");
				return null;
			}
		}
		logger.trace("data waiting done");
		WindowProperty raw = dataWindowProp;
		dataWindowProp = null;
		return raw;
	}

	// TODO need to implement Increment protocol, but for now we just use a very
	// large buffer
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
		if (logger.isTraceEnabled()) {
			logger.trace("Type: " + actualTypeReturn.getValue() + " Format: " + actualFormatReturn.getValue());
			logger.trace("nItems: " + nItemsReturn.getValue() + " bytesAfter: " + bytesAfterReturn.getValue());
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
			// works with UTF-8 or default
			str = property.getString(0, "UTF-8");
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

	/**
	 * just a storage class of the returns when we call XGetWindowProperty.
	 *
	 */
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
