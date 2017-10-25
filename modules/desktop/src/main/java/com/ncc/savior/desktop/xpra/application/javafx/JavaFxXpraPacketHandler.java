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
			return;
		}

		String name = packet.getName();
		Cursor cursor = null;
		// attempt to find a local system cursor
		if (cursorNameMap.containsKey(name)) {
			cursor = cursorNameMap.get(name);
		} else {
			try {
				System.out.println(name);
				cursor = Cursor.cursor(name);

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
		} else {

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
