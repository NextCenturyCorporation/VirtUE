package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;
import java.util.Collection;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;

/**
 * Message that indicates that the clipboard has changed on the source machine
 *
 *
 */
public class ClipboardChangedMessage extends BaseClipboardMessage implements Serializable
{
	private static final long serialVersionUID = 1L;

	public ClipboardChangedMessage(String sourceId, Collection<ClipboardFormat> formats) {
		super(sourceId);
		this.formats = formats;
	}

	private Collection<ClipboardFormat> formats;

	/**
	 * Returns the available formats on the clipboard.
	 *
	 * @return
	 */
	public Collection<ClipboardFormat> getFormats() {
		return formats;
	}

	@Override
	public String toString() {
		return "ClipboardChangedMessage [formats=" + formats + ", sendTime=" + sendTime + ", messageSourceId="
				+ messageSourceId + "]";
	}
}
