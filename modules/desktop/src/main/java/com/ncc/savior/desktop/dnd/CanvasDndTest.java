package com.ncc.savior.desktop.dnd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.swing.JCanvas;

public class CanvasDndTest {
	static boolean dragging = false;
	private static final Logger logger = LoggerFactory.getLogger(CanvasDndTest.class);

	public static void main(String[] args) {

		JCanvas canvas = new JCanvas(300, 300);

		Graphics g = canvas.getGraphics();
		g.setColor(Color.RED);
		g.fillRect(3, 3, 297, 297);

		JFrame frame = new JFrame();
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

		canvas.setTransferHandler(new MyTransferHandler(new Date().toString()));
		canvas.getTransferHandler().exportAsDrag(canvas, null, TransferHandler.MOVE);
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (dragging) {
					logger.debug("ending drag");
					dragging = false;
				} else {
					logger.debug("starting drag");
					// canvas.getTransferHandler().exportAsDrag(canvas, e, TransferHandler.MOVE);
					dragging = true;
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				logger.debug("release");
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});
		canvas.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// canvas.getTransferHandler().exportAsDrag(canvas, e, TransferHandler.MOVE);
				// logger.debug("release");
			}

			@Override
			public void mouseDragged(MouseEvent e) {

				logger.debug("dragged " + e.getX());
				if (e.getX() > 500) {
					canvas.getTransferHandler().exportAsDrag(canvas, e, TransferHandler.MOVE);
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				logger.debug("moved " + e.getX());
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public static class MyTransferHandler extends TransferHandler {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		public final DataFlavor SUPPORTED_DATE_FLAVOR = DataFlavor.stringFlavor;
		private String value;

		public MyTransferHandler(String value) {
			this.value = value;
		}

		protected void registerListeners() {
		}

		public String getValue() {
			return value;
		}

		@Override
		public int getSourceActions(JComponent c) {
			// if (dragging) {
				return DnDConstants.ACTION_COPY_OR_MOVE;
			// } else {
			// return 0;
			// }
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			logger.debug("creating transferable");
			Transferable t = new StringSelection(getValue());
			return t;
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			logger.debug("can import " + support.getDropLocation().getDropPoint().getX());

			return true;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			super.exportDone(source, data, action);
			// Decide what to do after the drop has been accepted
		}

	}
}
