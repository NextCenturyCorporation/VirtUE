package com.ncc.savior.desktop.xpra.application.javafx;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.CursorPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;

/**
 * This class handles packets that aren't directly related to specific windows.
 *
 *
 */
public class JavaFxXpraPacketHandler implements IPacketHandler {
	private static Logger logger = LoggerFactory.getLogger(JavaFxXpraPacketHandler.class);

	private HashSet<PacketType> types;
	private Scene scene;
	private Map<String, Cursor> cursorNameMap;

	public JavaFxXpraPacketHandler(Scene scene) {
		types = new HashSet<PacketType>();
		types.add(PacketType.CURSOR);
		this.scene = scene;
		this.cursorNameMap = new HashMap<String, Cursor>();
		this.cursorNameMap.put("hand2", Cursor.HAND);
		this.cursorNameMap.put("left_ptr", Cursor.DEFAULT);
		this.cursorNameMap.put("xterm", Cursor.TEXT);
		this.cursorNameMap.put("sb_h_double_arrow", Cursor.H_RESIZE);
		this.cursorNameMap.put("sb_v_double_arrow", Cursor.V_RESIZE);
		this.cursorNameMap.put("col-resize", Cursor.H_RESIZE);
		this.cursorNameMap.put("row-resize", Cursor.V_RESIZE);
		this.cursorNameMap.put("n-resize", Cursor.N_RESIZE);
		this.cursorNameMap.put("s-resize", Cursor.S_RESIZE);
		this.cursorNameMap.put("w-resize", Cursor.W_RESIZE);
		this.cursorNameMap.put("e-resize", Cursor.E_RESIZE);
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
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					scene.setCursor(Cursor.DEFAULT);
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
				cursor = Cursor.cursor(name);
				if (logger.isTraceEnabled()) {
					logger.trace("Found cursor by name.  name=" + name + " cursor=" + cursor + " Packet=" + packet);
				}

			} catch (IllegalArgumentException e) {
			}
		}
		if (cursor != null) {
			Cursor c = cursor;
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					scene.setCursor(c);
				}
			});
		} else if (packet.getBytes() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Cursor not found.  Attempting to build from bytes name=" + name + " bytes="
						+ packet.getBytes() + " Packet=" + packet);
			}
			try {
				BufferedImage bimg = cursorPacketToBufferedImage(packet);

				WritableImage img = new WritableImage(packet.getWidth(), packet.getHeight());
				img = SwingFXUtils.toFXImage(bimg, img);

				final ImageCursor imgCursor = new ImageCursor(img, packet.getxHotspot(), packet.getyHotspot());

				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						scene.setCursor(imgCursor);
					}
				});
			} catch (RuntimeException e) {
				logger.warn("Failed to draw cursor from image.  Packet=" + packet);
			}
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("Unable to create cursor.  Setting default.  Packet=" + packet);
			}
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					scene.setCursor(Cursor.DEFAULT);
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
