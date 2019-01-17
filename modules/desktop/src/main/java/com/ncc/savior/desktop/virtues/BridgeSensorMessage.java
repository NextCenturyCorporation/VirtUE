package com.ncc.savior.desktop.virtues;

public class BridgeSensorMessage {

	private String message;
	private String username;
	private long date;

	private MessageType messageType;

	public BridgeSensorMessage(String message, String username, MessageType messageType) {
		this.message = message;
		this.username = username;
		this.date = System.currentTimeMillis();
		this.messageType = messageType;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

}