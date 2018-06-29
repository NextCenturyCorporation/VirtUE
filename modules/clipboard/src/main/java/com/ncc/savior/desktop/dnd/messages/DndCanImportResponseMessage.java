package com.ncc.savior.desktop.dnd.messages;

import com.ncc.savior.desktop.clipboard.messages.BaseClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.MessageType;

public class DndCanImportResponseMessage extends BaseClipboardMessage {

	private static final long serialVersionUID = 1L;
	private String destId;
	private boolean allowed;
	private String requestId;

	public DndCanImportResponseMessage(String sourceId, String destId, String requestId, boolean allowed) {
		super(sourceId, MessageType.DND);
		this.destId = destId;
		this.allowed = allowed;
		this.requestId = requestId;
	}

	public String getDestId() {
		return destId;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public String getRequestId() {
		return requestId;
	}

	@Override
	public String toString() {
		return "DndCanImportResponseMessage [destId=" + destId + ", allowed=" + allowed + ", sendTime=" + sendTime
				+ ", messageSourceId=" + messageSourceId + "]";
	}

}
