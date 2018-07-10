package com.ncc.savior.desktop.dnd;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.dnd.messages.DndCanImportResponseMessage;
import com.ncc.savior.desktop.dnd.messages.DndDataRequestMessage;
import com.ncc.savior.desktop.dnd.messages.DndDataResponseMessage;
import com.ncc.savior.desktop.dnd.messages.DndStartDragMessage;

public class DndBackdrop extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DndBackdrop.class);
	private IClipboardMessageSenderReceiver transmitter;
	private String messageSourceId;
	public Map<String, CompletableFuture> futureMap;
	private JTextArea allBox;

	public DndBackdrop(IClipboardMessageSenderReceiver transmitter) {
		futureMap = Collections.synchronizedMap(new HashMap<String, CompletableFuture>());
		this.transmitter = transmitter;
		IClipboardMessageHandler dndMessageHandler = new IClipboardMessageHandler() {

			@Override
			public void onMessageError(String description, IOException e) {
				DndBackdrop.this.dispose();
				logger.error("Message error in Drag and drop: " + description, e);
			}

			@Override
			public void onMessage(IClipboardMessage message, String groupId) {
				logger.debug("DND Got Message: " + message);
				if (message instanceof DndCanImportResponseMessage) {
					DndCanImportResponseMessage m = (DndCanImportResponseMessage) message;
					String key = m.getRequestId();
					@SuppressWarnings("unchecked")
					CompletableFuture<Boolean> future = futureMap.remove(key);
					logger.debug("future: " + future);
					if (future != null && !future.isDone()) {
						boolean success = future.complete(((DndCanImportResponseMessage) message).isAllowed());
						logger.debug("Future completed success=" + success + " key: " + key + " f: " + future);
					} else {
						logger.debug("unable to complete future " + future);
					}
				} else if (message instanceof DndDataRequestMessage) {
					logger.debug("***DATA REQUEST***");

					DndDataRequestMessage m = (DndDataRequestMessage) message;
					DndDataResponseMessage response = new DndDataResponseMessage(transmitter.getClientId(), m,
							"remoteHardCodedTextData");
					try {
						transmitter.sendMessageToHub(response);
					} catch (IOException e) {
						logger.debug("failed to send message", e);
					}
				}
			}

			@Override
			public void closed() {
				logger.warn("Transmitter closed");
			}
		};
		transmitter.setDndMessageHandler(dndMessageHandler);
		this.messageSourceId = transmitter.getClientId();
		logger.debug("started dnd do not show");
		this.setTitle("DONOTSHOW");
		this.setSize(1600, 1200);
		this.setLocation(0, 0);
		this.setUndecorated(true);
		allBox = new JTextArea();
		this.getContentPane().add(allBox, BorderLayout.CENTER);

		allBox.setDragEnabled(true);
		allBox.setDropMode(DropMode.INSERT);
		allBox.setTransferHandler(new ListTransferHandler());
	}

	class ListTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY_OR_MOVE;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Transferable createTransferable(JComponent source) {
			logger.debug("create transferable");
			JList<String> sourceList = (JList<String>) source;
			String data = sourceList.getSelectedValue();
			Transferable t = new StringSelection(data);
			return t;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			logger.debug("export done");
			@SuppressWarnings("unchecked")
			JList<String> sourceList = (JList<String>) source;
			String movedItem = sourceList.getSelectedValue();
			if (action == TransferHandler.MOVE) {
				DefaultListModel<String> listModel = (DefaultListModel<String>) sourceList.getModel();
				listModel.removeElement(movedItem);
			}
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			logger.debug("test");
			if (!support.isDrop()) {
				return false;
			}
			boolean ret = true;
			try {
				String requestId = UUID.randomUUID().toString();
				DndStartDragMessage message = new DndStartDragMessage(messageSourceId, requestId, support);
				int timeout = 3000;
				ret = sendMessageAndWaitForResponseWithTimeout(ret, requestId, message, timeout);
			} catch (IOException e) {
				logger.error("Drag and Drop canImport test failed", e);
			}
			// boolean ret = support.isDataFlavorSupported(DataFlavor.stringFlavor);
			logger.debug("test " + ret);
			return ret;
		}

		private <T> T sendMessageAndWaitForResponseWithTimeout(T timeoutValue, String requestId,
				IClipboardMessage message, int timeout) throws IOException {
			CompletableFuture<T> f = new CompletableFuture<T>();
			logger.debug("Adding future " + requestId + " : " + f);
			futureMap.put(requestId, f);
			logger.debug("Sending message to hub: " + "msg: " + message);
			transmitter.sendMessageToHub(message);
			logger.debug("Sent message to hub: " + "xmit: " + transmitter);
			T ret = timeoutValue;
			try {
				ret = f.get(timeout, TimeUnit.MILLISECONDS);
				logger.debug("received: " + ret);
			} catch (TimeoutException | InterruptedException | ExecutionException e) {
				logger.error("Drag and Drop canImport test did not complete succesfully", e);
				// we want the default
			} finally {
				// remove no matter what
				futureMap.remove(requestId);
			}
			return ret;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			logger.debug("GETTING DATA!!!!!!!!!!!!!!!!!!");
			if (!this.canImport(support)) {
				return false;
			}
			DataFlavor flavor = DataFlavor.stringFlavor;
			Transferable t = support.getTransferable();
			String data = null;
			try {
				data = (String) t.getTransferData(flavor);
				if (data == null) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			String requestId = UUID.randomUUID().toString();
			DndDataResponseMessage message = new DndDataResponseMessage(messageSourceId, flavor, requestId, data);
			try {
				transmitter.sendMessageToHub(message);
			} catch (IOException e) {
				logger.error("error failed to send data message=" + message, e);
				return false;
			}
			return true;
		}
	}
}
