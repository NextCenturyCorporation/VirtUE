package com.ncc.savior.desktop.virtues;

public class ClipboardBridgeSensorMessage extends BridgeSensorMessage {

	private String source;
	private String destination;

	public ClipboardBridgeSensorMessage(String message, String username, MessageType messageType, String source,
			String destination) {
		super(message, username, messageType);
		this.source = source;
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

}
