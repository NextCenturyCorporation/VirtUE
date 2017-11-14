package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		int[] screen = new int[] { 1200, 800 };
		String[] encodings = new String[] { "h264", "jpeg", "png", "png/P" };
		map.put(VERSION, XpraClient.VERSION);
		map.put(DESKTOP_SIZE, screen);
		map.put(DPI, 96);
		map.put(CLIENT_TYPE, CLIENT_TYPE_VALUE);
		map.put(SCREEN_SIZES, new int[][] { screen });
		map.put(ENCODINGS, encodings);
		map.put(ZLIB, true);
		map.put(CLIPBOARD, false);
		map.put(NOTIFICATIONS, true);
		map.put(CURSORS, true);
		map.put(NAMED_CURSORS, true);
		map.put(BELL, true);
		map.put(BENCODE, true);
		map.put(RENCODE, false);
		map.put(CHUNKED_COMPRESSION, true);
		map.put(ENCODING, encodings[0]);
		map.put(PLATFORM, System.getProperty("os.name").toLowerCase());
		map.put(CAPABILITY_UUID, UUID.randomUUID().toString().replace("-", ""));

		map.put(KEYBOARD, true);
		map.put(KEYBOARD_SYNC, false);
		map.put(XKBMAP_LAYOUT, Locale.getDefault().getLanguage());
		map.put(XKBMAP_VARIANT, Locale.getDefault().getVariant());

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
}
