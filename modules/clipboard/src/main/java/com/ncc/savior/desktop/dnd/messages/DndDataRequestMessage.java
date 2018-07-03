package com.ncc.savior.desktop.dnd.messages;

import java.awt.datatransfer.DataFlavor;

import com.ncc.savior.desktop.clipboard.messages.BaseClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.MessageType;

public class DndDataRequestMessage extends BaseClipboardMessage {

	private static final long serialVersionUID = 1L;
	private DataFlavor flavor;
	private String requestId;

	public DndDataRequestMessage(String messageSourceId, DataFlavor flavor, String requestId) {
		super(messageSourceId, MessageType.DND);
		this.flavor = flavor;
		this.requestId = requestId;
	}

	public DataFlavor getFlavor() {
		return flavor;
	}

	public String getRequestId() {
		return requestId;
	}
}
