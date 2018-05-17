package com.ncc.savior.desktop.clipboard.connection;

import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.util.JavaUtil;

public class ClipboardConnection implements IClipboardConnection {

	private IMessageSerializer serializer;

	public ClipboardConnection(IMessageSerializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public void close() {
		JavaUtil.closeIgnoreErrors(serializer);
	}

	@Override
	public IClipboardMessageSenderReceiver getMessagePasser() {
		// TODO Auto-generated method stub
		return null;
	}

}
