package com.ncc.savior.desktop.virtues;

public class ApplicationBridgeSensorMessage extends VirtueBridgeSensorMessage {

	private String applicationId;
	private String applicationName;

	public ApplicationBridgeSensorMessage(String message, String username, MessageType messageType,
			String virtueId, String virtueName, String applicationId, String applicationName) {
		super(message, username, messageType, virtueId, virtueName);
		this.applicationId = applicationId;
		this.applicationName = applicationName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

}
