package com.ncc.savior.desktop.virtues;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public class HyperlinkBridgeSensorMessage extends ClipboardBridgeSensorMessage {

	private DefaultApplicationType applicationType;
	private String params;

	public HyperlinkBridgeSensorMessage(String message, String username, MessageType messageType, String sourceGroupId,
			String destinationGroupId, DefaultApplicationType applicationType, String params) {
		super(message, username, messageType, sourceGroupId, destinationGroupId);
		this.applicationType = applicationType;
		this.params = params;
	}

	public DefaultApplicationType getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(DefaultApplicationType applicationType) {
		this.applicationType = applicationType;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

}
