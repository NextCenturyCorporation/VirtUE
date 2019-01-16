package com.ncc.savior.desktop.virtues;

public class ClipboardBridgeSensorMessage extends BridgeSensorMessage {

	private String sourceGroupId;
	private String destinationGroupId;

	public ClipboardBridgeSensorMessage(String message, String username, MessageType messageType, String sourceGroupId,
			String destinationGroupId) {
		super(message, username, messageType);
		this.sourceGroupId = sourceGroupId;
		this.destinationGroupId = destinationGroupId;
	}

	public String getSourceGroupId() {
		return sourceGroupId;
	}

	public void setSourceGroupId(String sourceGroupId) {
		this.sourceGroupId = sourceGroupId;
	}

	public String getDestinationGroupId() {
		return destinationGroupId;
	}

	public void setDestinationGroupId(String destinationGroupId) {
		this.destinationGroupId = destinationGroupId;
	}

}
