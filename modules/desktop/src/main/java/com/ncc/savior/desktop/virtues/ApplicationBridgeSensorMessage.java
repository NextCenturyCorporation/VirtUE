package com.ncc.savior.desktop.virtues;

public class ApplicationBridgeSensorMessage extends BridgeSensorMessage {

	private String applicationId;
	private String virtueId;

	public ApplicationBridgeSensorMessage(String message, String username, MessageType messageType,
			String applicationId, String virtueId) {
		super(message, username, messageType);
		this.applicationId = applicationId;
		this.virtueId = virtueId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}
}
