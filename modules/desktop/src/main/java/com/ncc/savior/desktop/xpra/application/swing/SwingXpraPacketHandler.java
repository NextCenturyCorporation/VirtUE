package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.HeadlessException;
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

import javax.swing.JFrame;
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
	private JFrame frame;

	private Map<String, Cursor> cursorNameMap;

	public SwingXpraPacketHandler(JFrame frame) {
		types = new HashSet<PacketType>();
		types.add(PacketType.CURSOR);
		this.frame = frame;
		this.cursorNameMap = new HashMap<String, Cursor>();
		this.cursorNameMap.put("hand2", Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.cursorNameMap.put("left_ptr", Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.cursorNameMap.put("xterm", Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		// this.cursorNameMap.put("sb_h_double_arrow", Cursor.H_RESIZE);
		// this.cursorNameMap.put("sb_v_double_arrow", Cursor.V_RESIZE);
		// this.cursorNameMap.put("col-resize", Cursor.H_RESIZE);
		// this.cursorNameMap.put("row-resize", Cursor.V_RESIZE);
		// this.cursorNameMap.put("n-resize", Cursor.N_RESIZE);
		// this.cursorNameMap.put("s-resize", Cursor.S_RESIZE);
		// this.cursorNameMap.put("w-resize", Cursor.W_RESIZE);
		// this.cursorNameMap.put("e-resize", Cursor.E_RESIZE);
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
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			});
			return;
		}
		String name = packet.getName();
		Cursor cursor = null;
		// attempt to find a local system cursor
		if (cursorNameMap.containsKey(name)) {
			cursor = cursorNameMap.get(name);
			if (logger.isTraceEnabled()) {
				logger.trace("Found cursor in cursor map.  name=" + name + " cursor=" + cursor + " Packet=" + packet);
			}
		} else {
			try {
				// System.out.println(name);
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
					frame.setCursor(c);
				}
			});
		} else if (packet.getBytes() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Cursor not found.  Attempting to build from bytes name=" + name + " bytes="
						+ packet.getBytes() + " Packet=" + packet);
			}
			try {
				BufferedImage bimg = cursorPacketToBufferedImage(packet);
				Toolkit kit = Toolkit.getDefaultToolkit();
				Cursor imgCursor = kit.createCustomCursor(bimg, new Point(packet.getxHotspot(), packet.getyHotspot()),
						packet.getName());

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						frame.setCursor(imgCursor);
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
					frame.setCursor(Cursor.getDefaultCursor());
				}
			});
		}
	}

	private BufferedImage cursorPacketToBufferedImage(CursorPacket packet) {
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
		return img;
	}

	@Override
	public Set<PacketType> getValidPacketTypes() {
		return types;
	}

}