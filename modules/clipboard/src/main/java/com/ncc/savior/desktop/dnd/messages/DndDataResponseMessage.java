package com.ncc.savior.desktop.dnd.messages;

import java.awt.datatransfer.DataFlavor;

import com.ncc.savior.desktop.clipboard.messages.BaseClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.MessageType;

public class DndDataResponseMessage extends BaseClipboardMessage {

	private static final long serialVersionUID = 1L;
	private DataFlavor flavor;
	private String requestId;
	private Object data;

	public DndDataResponseMessage(String messageSourceId, DataFlavor flavor, String requestId, Object data) {
		super(messageSourceId, MessageType.DND);
		this.flavor = flavor;
		this.requestId = requestId;
		this.data = data;
	}

	public DndDataResponseMessage(String messageSourceId, DndDataRequestMessage request, Object data) {
		super(messageSourceId, MessageType.DND);
		this.flavor = request.getFlavor();
		this.requestId = request.getRequestId();
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public DataFlavor getFlavor() {
		return flavor;
	}

	public String getRequestId() {
		return requestId;
	}

	@Override
	public String toString() {
		return "DndDataResponseMessage [flavor=" + flavor + ", requestId=" + requestId + ", data=" + data
				+ ", sendTime=" + sendTime + ", messageSourceId=" + messageSourceId + "]";
	}
}
