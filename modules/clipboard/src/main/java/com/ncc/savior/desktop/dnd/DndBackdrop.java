package com.ncc.savior.desktop.dnd;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.MessageTransmitter;


public class DndBackdrop extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DndBackdrop.class);
	private MessageTransmitter transmitter;

	public DndBackdrop(MessageTransmitter transmitter) {
		this.transmitter = transmitter;
		logger.debug("started dnd do not show");
		this.setTitle("DONOTSHOW");
		this.setSize(1600, 1200);
		this.setLocation(0, 0);
		this.setUndecorated(true);
		JTextArea allBox = new JTextArea();
		this.getContentPane().add(allBox, BorderLayout.CENTER);

		allBox.setDragEnabled(true);
		allBox.setDropMode(DropMode.INSERT);
		allBox.setTransferHandler(new ListTransferHandler());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			DndBackdrop frame = new DndBackdrop(null);
			// frame.pack();
			frame.setVisible(true);
		});
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
			boolean ret = support.isDataFlavorSupported(DataFlavor.stringFlavor);
			logger.debug("test " + ret);
			return ret;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			logger.debug("do import");
			if (!this.canImport(support)) {
				return false;
			}
			Transferable t = support.getTransferable();
			String data = null;
			try {
				data = (String) t.getTransferData(DataFlavor.stringFlavor);
				if (data == null) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			JTextComponent.DropLocation dropLocation = (JTextArea.DropLocation) support.getDropLocation();
			int dropIndex = dropLocation.getIndex();
			JTextComponent textComp = (JTextComponent) support.getComponent();
			String text = textComp.getText();
			String newText = text;
			if (dropIndex != -1) {
				try {
					newText = text.substring(0, dropIndex);
					newText += data;
					if (dropIndex < text.length()) {
						newText += text.substring(dropIndex + 1, text.length());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			textComp.setText(newText);
			return true;
		}
	}
}
