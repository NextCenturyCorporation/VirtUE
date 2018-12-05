package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.CursorPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * This class handles packets that aren't directly related to specific windows.
 *
 *
 */
public class SwingXpraPacketHandler implements IPacketHandler {
	private static Logger logger = LoggerFactory.getLogger(SwingXpraPacketHandler.class);

	private HashSet<PacketType> types;
	private WindowFrame frame;

	private Map<String, Cursor> cursorNameMap;

	public SwingXpraPacketHandler(WindowFrame frame) {
		types = new HashSet<PacketType>();
		types.add(PacketType.CURSOR);
		this.frame = frame;
		this.cursorNameMap = new HashMap<String, Cursor>();
		this.cursorNameMap.put("hand2", Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.cursorNameMap.put("left_ptr", Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.cursorNameMap.put("xterm", Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		this.cursorNameMap.put("sb_h_double_arrow", Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		this.cursorNameMap.put("sb_v_double_arrow", Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		// this.cursorNameMap.put("col-resize", Cursor.getPredefinedCursor(Cursor.);
		// this.cursorNameMap.put("row-resize", Cursor.getPredefinedCursor(Cursor.);
		this.cursorNameMap.put("text", Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		this.cursorNameMap.put("n-resize", Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		this.cursorNameMap.put("s-resize", Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		this.cursorNameMap.put("w-resize", Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		this.cursorNameMap.put("e-resize", Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		this.cursorNameMap.put("ne-resize", Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
		this.cursorNameMap.put("se-resize", Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		this.cursorNameMap.put("nw-resize", Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
		this.cursorNameMap.put("sw-resize", Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
	}

	@Override
	public void handlePacket(Packet packet) {
		switch (packet.getType()) {
		case CURSOR:
			handleCursorPacket((CursorPacket) packet);
			break;
		default:

		}

	}

	private void handleCursorPacket(CursorPacket packet) {
		if (packet.isEmpty()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			});
			return;
		}
		String name = packet.getName();
		Cursor cursor = null;
		// logger.info("CURSOR: " + name + " : " + packet);
		// attempt to find a local system cursor
		if (cursorNameMap.containsKey(name)) {
			cursor = cursorNameMap.get(name);
			if (logger.isTraceEnabled()) {
				logger.trace("Found cursor in cursor map.  name=" + name + " cursor=" + cursor + " Packet=" + packet);
			}
		} else {
			try {
				cursor = Cursor.getSystemCustomCursor(name);
				if (logger.isTraceEnabled()) {
					logger.trace("Found cursor by name.  name=" + name + " cursor=" + cursor + " Packet=" + packet);
				}

			} catch (IllegalArgumentException e) {
			} catch (HeadlessException e) {
			} catch (AWTException e) {
			}
		}
		if (cursor != null) {
			Cursor c = cursor;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					frame.getWindow().setCursor(c);
				}
			});
		} else if (packet.getBytes() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Cursor not found.  Attempting to build from bytes name=" + name + " bytes="
						+ packet.getBytes() + " Packet=" + packet);
			}
			try {
				// For some reason, Xpra version 2.4.2 no longer sends named cursors, like
				// "pointer", but instead only sends images for the cursors. The ones that get
				// sent are too big and look terrible. This just makes them look somewhat
				// reasonable.
				Image bimg = cursorPacketToBufferedImage(packet);
				int dim = Math.max(bimg.getWidth(null), bimg.getHeight(null));
				float mult = 1.5f;
				dim *= mult;
				BufferedImage bi = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
				bi.getGraphics().drawImage(bimg, 0, 0, null);
				Toolkit kit = Toolkit.getDefaultToolkit();
				int xhot = (int) (packet.getxHotspot() * mult);
				int yhot = (int) (packet.getyHotspot() * mult);
				Cursor imgCursor = kit.createCustomCursor(bi, new Point(xhot, yhot), packet.getName());

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						frame.getWindow().setCursor(imgCursor);
					}
				});
			} catch (RuntimeException e) {
				logger.warn("Failed to draw cursor from image.  Packet=" + packet);
			}
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("Unable to create cursor.  Setting default.  Packet=" + packet);
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.getWindow().setCursor(Cursor.getDefaultCursor());
				}
			});
		}
	}

	private Image cursorPacketToBufferedImage(CursorPacket packet) {
		int width = packet.getWidth();
		int height = packet.getHeight();
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		int[] nBits = { 8, 8, 8, 8 };
		int[] bOffs = { 1, 2, 3, 0 };
		ColorModel colorModel = new ComponentColorModel(cs, nBits, true, true, Transparency.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * 4, 4, bOffs,
				null);

		BufferedImage img = new BufferedImage(colorModel, raster, true, null);
		img.getRaster().setDataElements(0, 0, width, height, packet.getBytes());
		// return img.getScaledInstance(32, 32, 0);
		return img;
	}

	@Override
	public Set<PacketType> getValidPacketTypes() {
		return types;
	}

}
