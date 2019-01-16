package com.ncc.savior.desktop.virtues;

public class VirtueBridgeSensorMessage extends BridgeSensorMessage {

	private String virtueId;

	public VirtueBridgeSensorMessage(String message, String username, MessageType messageType, String virtueId) {
		super(message, username, messageType);
		this.virtueId = virtueId;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}

}
