package com.ncc.savior.desktop.clipboard.connection;

import java.io.Closeable;

import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;

public interface IClipboardConnection extends Closeable {
	@Override
	public void close();

	public IClipboardMessageSenderReceiver getMessagePasser();

}
