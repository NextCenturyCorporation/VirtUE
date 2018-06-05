package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;
import java.util.Set;

/**
 * Message that indicates that the clipboard has changed on the source machine
 *
 *
 */
public class ClipboardChangedMessage extends BaseClipboardMessage implements Serializable
{
	private static final long serialVersionUID = 1L;

	public ClipboardChangedMessage(String sourceId, Set<Integer> formats) {
		super(sourceId);
		this.formats = formats;
	}

	private Set<Integer> formats;

	/**
	 * Returns the available formats on the clipboard.
	 *
	 * @return
	 */
	public Set<Integer> getFormats() {
		return formats;
	}

	@Override
	public String toString() {
		return "ClipboardChangedMessage [formats=" + formats + ", sendTime=" + sendTime + ", messageSourceId="
				+ messageSourceId + "]";
	}
}
