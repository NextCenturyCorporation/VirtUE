package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by the server to indicate that a recent click has initiated a
 * move or resize. This is typically done when the click is on the title bar
 * (move) or on one of the edges of the window (resize).
 *
 *
 */
public class InitiateMoveResizePacket extends WindowPacket {

	private int xRoot;
	private int yRoot;
	private int direction;
	private int button;
	private int sourceIndication;

	public InitiateMoveResizePacket(int windowId, int xRoot, int yRoot, int direction, int button,
			int sourceIndication) {
		super(windowId, PacketType.INITIATE_MOVERESIZE);
		this.xRoot = xRoot;
		this.yRoot = yRoot;
		this.direction = direction;
		this.button = button;
		this.sourceIndication = sourceIndication;
	}

	public InitiateMoveResizePacket(List<Object> list) {
		this(PacketUtils.asInt(list, 1), PacketUtils.asInt(list, 2), PacketUtils.asInt(list, 3),
				PacketUtils.asInt(list, 4), PacketUtils.asInt(list, 5), PacketUtils.asInt(list, 6));
	}

	public int getxRoot() {
		return xRoot;
	}

	public int getyRoot() {
		return yRoot;
	}

	public int getDirectionInt() {
		return direction;
	}

	public MoveResizeDirection getDirection() {
		return MoveResizeDirection.INDEX[direction];
	}

	public int getButton() {
		return button;
	}

	public int getSourceIndication() {
		return sourceIndication;
	}

	@Override
	protected void doAddToList(List<Object> list) {
		System.out.println(list);

	}

	@Override
	public String toString() {
		return "InitiateMoveResizePacket [xRoot=" + xRoot + ", yRoot=" + yRoot + ", direction="
				+ getDirection().toString() + ", button=" + button + ", sourceIndication=" + sourceIndication
				+ ", windowId=" + windowId + ", type=" + type + "]";
	}

	public enum MoveResizeDirection {
		MOVERESIZE_SIZE_TOPLEFT(0), MOVERESIZE_SIZE_TOP(1), MOVERESIZE_SIZE_TOPRIGHT(2), MOVERESIZE_SIZE_RIGHT(
				3), MOVERESIZE_SIZE_BOTTOMRIGHT(4), MOVERESIZE_SIZE_BOTTOM(5), MOVERESIZE_SIZE_BOTTOMLEFT(
						6), MOVERESIZE_SIZE_LEFT(7), MOVERESIZE_MOVE(
								8), MOVERESIZE_SIZE_KEYBOARD(9), MOVERESIZE_MOVE_KEYBOARD(10), MOVERESIZE_CANCEL(11);
		// private int num;

		private static MoveResizeDirection[] INDEX = MoveResizeDirection.values();

		MoveResizeDirection(int i) {
			// this.num = i;
		}
	}
	// #initiate-moveresize X11 constants
	// MOVERESIZE_SIZE_TOPLEFT = 0
	// MOVERESIZE_SIZE_TOP = 1
	// MOVERESIZE_SIZE_TOPRIGHT = 2
	// MOVERESIZE_SIZE_RIGHT = 3
	// MOVERESIZE_SIZE_BOTTOMRIGHT = 4
	// MOVERESIZE_SIZE_BOTTOM = 5
	// MOVERESIZE_SIZE_BOTTOMLEFT = 6
	// MOVERESIZE_SIZE_LEFT = 7
	// MOVERESIZE_MOVE = 8
	// MOVERESIZE_SIZE_KEYBOARD = 9
	// MOVERESIZE_MOVE_KEYBOARD = 10
	// MOVERESIZE_CANCEL = 11
	// MOVERESIZE_DIRECTION_STRING = {
	// MOVERESIZE_SIZE_TOPLEFT : "SIZE_TOPLEFT",
	// MOVERESIZE_SIZE_TOP : "SIZE_TOP",
	// MOVERESIZE_SIZE_TOPRIGHT : "SIZE_TOPRIGHT",
	// MOVERESIZE_SIZE_RIGHT : "SIZE_RIGHT",
	// MOVERESIZE_SIZE_BOTTOMRIGHT : "SIZE_BOTTOMRIGHT",
	// MOVERESIZE_SIZE_BOTTOM : "SIZE_BOTTOM",
	// MOVERESIZE_SIZE_BOTTOMLEFT : "SIZE_BOTTOMLEFT",
	// MOVERESIZE_SIZE_LEFT : "SIZE_LEFT",
	// MOVERESIZE_MOVE : "MOVE",
	// MOVERESIZE_SIZE_KEYBOARD : "SIZE_KEYBOARD",
	// MOVERESIZE_MOVE_KEYBOARD : "MOVE_KEYBOARD",
	// MOVERESIZE_CANCEL : "CANCEL",
	// }
	// SOURCE_INDICATION_UNSET = 0
	// SOURCE_INDICATION_NORMAL = 1
	// SOURCE_INDICATION_PAGER = 2
	// SOURCE_INDICATION_STRING = {
	// SOURCE_INDICATION_UNSET : "UNSET",
	// SOURCE_INDICATION_NORMAL : "NORMAL",
	// SOURCE_INDICATION_PAGER : "PAGER",
	// }
}
