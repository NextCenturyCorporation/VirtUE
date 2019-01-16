package com.ncc.savior.desktop.virtues;

public class VirtueBridgeSensorMessage extends BridgeSensorMessage {

	private String virtueId;
	private String virtueName;

	public VirtueBridgeSensorMessage(String message, String username, MessageType messageType, String virtueId,
			String virtueName) {
		super(message, username, messageType);
		this.virtueId = virtueId;
		this.virtueName = virtueName;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}

	public String getVirtueName() {
		return virtueName;
	}

	public void setVirtueName(String virtueName) {
		this.virtueName = virtueName;
	}

}
