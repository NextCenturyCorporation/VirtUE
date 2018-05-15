package com.ncc.savior.desktop.clipboard.messages;

import java.util.Date;

public interface IClipboardMessage {
	Date getSendTime();

	String getClipboardOwnerId();
}
