package com.ncc.savior.desktop.xpra.protocol.packet;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.CloseWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.CursorPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DamageSequencePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.FocusPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.HelloPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.PingEchoPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.PingPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.ScrollPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.SetDeflatePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.StartupCompletePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.UnknownPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowIconPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadataPacket;

/**
 * All the PacketTypes found in the Xpra documentation. Implemented classes
 * should have a class attached to their value here.
 *
 * list found https://xpra.org/trac/wiki/NetworkProtocol
 */
public enum PacketType {
	// @formatter:off
	// General packets that can flow in both directions (Server -> Client and Client
	// -> Server)
    HELLO("hello", HelloPacket.class),
	PING("ping", PingPacket.class), PING_ECHO("ping_echo", PingEchoPacket.class),
    SOUND_DATA("sound-data"),
    CONNECTION_LOST("connection-lost"),
    GIBBERISH("gibberish"),
	SET_DEFLATE("set_deflate", SetDeflatePacket.class),
    SET_CLIPBOARD_ENABLED(""),
    DESKTOP_SIZE(""),
	// Packets that are sent by the Server to the Client
	STARTUP_COMPLETE("startup-complete", StartupCompletePacket.class),
	NEW_WINDOW("new-window", NewWindowPacket.class),
    NEW_WINDOW_OVERRIDE_REDIRECT("new-override-redirect", NewWindowOverrideRedirectPacket.class),
    NEW_TRAY("new-tray"),
    LOST_WINDOW("lost-window", LostWindowPacket.class),
    RAISE_WINDOW("raise-window"),
    CONFIGURE_OVERRIDE_REDIRECT("configure-override-redirect"),
    WINDOW_MOVE_RESIZE("window-move-resize"),
    WINDOW_RESIZED("window-resized"),
    WINDOW_ICON("window-icon", WindowIconPacket.class),
    WINDOW_METADATA("window-metadata",WindowMetadataPacket.class),
    CURSOR("cursor", CursorPacket.class),
    BELL("bell"),
    NOTIFY_SHOW("notify-show"),
    NOTIFY_CLOSE("notify-close"),
	DRAW("draw", DrawPacket.class),
    RPC_REPLY("rpc-reply"),
    CONTROL("control"),
    INFO_RESPONSE("info-response"),
	// Packets that are sent by the Client to the Server
    SCROLL("scroll", ScrollPacket.class),
    SET_KEYBOARD_SYNC_ENABLED("set-keyboard-sync-enabled"),
    SET_CURSORS("set-cursors"),
    SET_NOTIFY("set-notify"),
    SET_BELL("set-bell"),
    MAP_WINDOW("map-window", MapWindowPacket.class),
    UNMAP_WINDOW("unmap-window"),
    CONFIGURE_WINDOW("configure-window"),
    CLOSE_WINDOW("close-window", CloseWindowPacket.class),
    FOCUS("focus", FocusPacket.class),
    BUTTON_ACTION("button-action"),
    POINTER_POSITION("pointer-position"),
    KEY_ACTION("key-action"),
    KEY_REPEAT("key-repeat"),
    LAYOUT_CHANGED("layout-changed"),
    KEYMAP_CHANGED("keymap-changed"),
	DAMAGE_SEQUENCE("damage-sequence", DamageSequencePacket.class),
    SERVER_SETTINGS("server-settings"),
    JPEG_QUALITY("jpeg-quality"),
    QUALITY("quality"),
    MIN_QUALITY("min-quality"),
    SPEED("speed"),
    MIN_SPEED("min-speed"),
    INFO_REQUEST("info-request"),
    SUSPEND("suspend"),
    RESUME("resume"),
    ENCODING("encoding"),
    BUFFER_REFRESH("buffer-refresh"),
    RPC("rpc"),
    SOUND_CONTROL("sound-control"),
    SHUTDOWN_SERVER("shutdown-server"),
    EXIT_SERVER("exit-server"),
    DISCONNECT("disconnect"),
	SCREENSHOT("screenshot"),
	UNKNOWN("UNKNOWN", UnknownPacket.class);
	// @formatter:on

	private String label;
	private Class<? extends Packet> klass;

	PacketType(String label) {
		this.label = label;
	}

	PacketType(String label, Class<? extends Packet> klass) {
		this.label = label;
		this.klass = klass;
	}

	public String getLabel() {
		return label;
	}

	public Class<? extends Packet> getPacketClass() {
		return klass;
	}

	public static PacketType getPacketType(String val) {
		for (PacketType type : PacketType.values()) {
			if (type.getLabel().equals(val)) {
				return type;
			}
		}
		return null;
	}
}
