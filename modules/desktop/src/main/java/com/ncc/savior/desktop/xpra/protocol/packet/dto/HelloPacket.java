/*
 * Copyright (C) 2019 Next Century Corporation
 *
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyMap;
import com.ncc.savior.desktop.xpra.protocol.keyboard.KeyCodeDto;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet used for initial handshaking. Client should send this Packet initially
 * with all of its capabilities and properties. The server will send another
 * {@link HelloPacket} as a response.
 *
 *
 */
public class HelloPacket extends Packet {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(HelloPacket.class);

	private static final String VERSION = "version";
	private static final String DESKTOP_SIZE = "desktop_size";
	private static final String CAPABILITY_UUID = "uuid";
	private static final String CLIENT_TYPE = "client_type";
	private static final String CLIENT_TYPE_VALUE = "java";
	private static final String DPI = "dpi";
	private static final String SCREEN_SIZES = "screen_sizes";
	private static final String ENCODINGS = "encodings";
	private static final String ENCODING = "encoding";
	private static final String ZLIB = "zlib";
	private static final String CLIPBOARD = "clipboard";
	private static final String NOTIFICATIONS = "notifications";
	private static final String CURSORS = "cursors";
	private static final String NAMED_CURSORS = "named_cursors";
	private static final String BELL = "bell";
	private static final String BENCODE = "bencode";
	private static final String RENCODE = "rencode";
	private static final String CHUNKED_COMPRESSION = "chunked_compression";
	private static final String PLATFORM = "platform";
	private static final String KEYBOARD = "keyboard";
	private static final String KEYBOARD_SYNC = "keyboard_sync";
	private static final String XKBMAP_LAYOUT = "xkbmap_layout";
	private static final String XKBMAP_VARIANT = "xkbmap_variant";
	private static final String XKBMAP_KEYCODES = "xkbmap_keycodes";
	private static final String SERVER_WINDOW_MOVE_RESIZE = "server-window-move-resize";
	private static final String SERVER_WINDOW_RESIZE = "server-window-resize";

	private Map<String, Object> map;

	protected HelloPacket(Map<String, Object> map) {
		super(PacketType.HELLO);
		this.map = map;
	}

	public HelloPacket(List<Object> list) {
		this(PacketUtils.asStringObjectMap(list, 1));

	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(map);
	}

	public static HelloPacket createDefaultRequest() {
		double maxX = 0;
		double maxY = 0;
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		for (GraphicsDevice device : devices) {
			Rectangle bounds = device.getDefaultConfiguration().getBounds();
			if (bounds.getMaxX() > maxX) {
				maxX = bounds.getMaxX();
			}
			if (bounds.getMaxY() > maxY) {
				maxY = bounds.getMaxY();
			}
		}

		LOGGER.debug("max size: " + maxX + "x" + maxY);
		int[] screen = new int[] { (int) maxX, (int) maxY };
		// removing h264 from this list causes the server to use RGB. I have no idea
		// why.
		String[] encodings = new String[] { "jpeg", "png", "png/P", "h264" };
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(VERSION, XpraClient.VERSION);
		map.put(DESKTOP_SIZE, screen);
		map.put(DPI, Toolkit.getDefaultToolkit().getScreenResolution());
		map.put(CLIENT_TYPE, CLIENT_TYPE_VALUE);
		int[][] screenSizes = getMonitorSizes();
		map.put(SCREEN_SIZES, screenSizes);
		map.put(ENCODINGS, encodings);
		map.put(ZLIB, true);
		map.put(CLIPBOARD, false);
		map.put(NOTIFICATIONS, true);
		map.put(CURSORS, true);
		map.put(NAMED_CURSORS, true);
		map.put(SERVER_WINDOW_MOVE_RESIZE, true);
		map.put(SERVER_WINDOW_RESIZE, true);
		map.put(BELL, true);
		map.put(BENCODE, true);
		map.put(RENCODE, false);
		map.put(CHUNKED_COMPRESSION, true);
		map.put(ENCODING, encodings[1]);
		map.put(PLATFORM, System.getProperty("os.name").toLowerCase());
		map.put(CAPABILITY_UUID, UUID.randomUUID().toString().replace("-", ""));

		map.put(KEYBOARD, true);
		map.put(KEYBOARD_SYNC, false);
		map.put(XKBMAP_LAYOUT, Locale.getDefault().getLanguage());
		map.put(XKBMAP_VARIANT, Locale.getDefault().getVariant());

		map.put("wants_events", true);
		map.put("share", true);
		map.put("generic_window_types", true);
		map.put("window.initiate-moveresize", true);

		Map<String, Object> windowMap = new HashMap<String, Object>();
		windowMap.put("raise", true);
		windowMap.put("initiate-moveresize", true);
		windowMap.put("resize-counter", true);

		// map.put("clipboard.want_targets", true);
		// map.put("clipboard.greedy", true);
		// String[] clipboards = new String[] { "CLIPBOARD", "PRIMARY" };
		// map.put("clipboard.selections", clipboards);
		map.put("window", windowMap);

		// map.put(XKBMAP_LAYOUT, "");
		// map.put(XKBMAP_VARIANT, "");

		// keyCodes are a list of list {int keyval, String keyname, int keycode, int
		// group, int level}

		return new HelloPacket(map);
	}

	@Override
	public String toString() {
		return "HelloPacket{" + "type=" + type + ", map=" + map + '}';
	}

	public void setKeyMap(IKeyMap keyMap) {
		Collection<KeyCodeDto> keycodes = keyMap.getKeyCodes();

		List<List<Object>> list = new ArrayList<List<Object>>();
		for (KeyCodeDto kcc : keycodes) {
			list.add(kcc.toList());
		}

		map.put(XKBMAP_KEYCODES, list);

	}

	private static int[][] getMonitorSizes() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice defaultDevice = ge.getDefaultScreenDevice();
		int[][] monitors = new int[gs.length][];
		for (int i = 0; i < gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			int[] display = new int[] { dm.getWidth(), dm.getHeight() };
			// sb.append(i + ", width: " + dm.getWidth() + ", height: " + dm.getHeight() +
			// "\n");
			LOGGER.debug("monitor " + i + ": " + dm.getWidth() + " x " + dm.getHeight()
					+ (gs[i] == defaultDevice ? " (default)" : ""));
			monitors[i] = display;
		}
		return monitors;
	}
}