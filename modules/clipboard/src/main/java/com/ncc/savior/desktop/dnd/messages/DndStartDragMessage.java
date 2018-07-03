package com.ncc.savior.desktop.dnd.messages;

import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler.TransferSupport;

import com.ncc.savior.desktop.clipboard.messages.BaseClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.MessageType;

public class DndStartDragMessage extends BaseClipboardMessage {

	private static final long serialVersionUID = 1L;
	private String requestId;
	private DataFlavor[] flavors;

	public DndStartDragMessage(String clientId, String requestId, TransferSupport support) {
		super(clientId, MessageType.DND);
		this.requestId = requestId;
		this.flavors = support.getDataFlavors();
	}

	public String getRequestId() {
		return requestId;
	}

	public DataFlavor[] getFlavors() {
		return flavors;
	}

	@Override
	public String toString() {
		return "DndCanImportRequestMessage [requestId=" + requestId + ", flavors=" + flavors.length + ", sendTime="
				+ sendTime + ", messageSourceId=" + messageSourceId + "]";
	}

}
