package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;
import java.util.Set;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;

/**
 * Message that indicates that the clipboard has changed on the source machine
 *
 *
 */
public class ClipboardChangedMessage extends BaseClipboardMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private Set<ClipboardFormat> formats;

	// For Jackson (de)serialization
	protected ClipboardChangedMessage() {
		this(null, null);
	}

	public ClipboardChangedMessage(String sourceId, Set<ClipboardFormat> formats) {
		super(sourceId);
		this.formats = formats;
	}

	/**
	 * Returns the available formats on the clipboard.
	 *
	 * @return
	 */
	public Set<ClipboardFormat> getFormats() {
		return formats;
	}

	@Override
	public String toString() {
		return "ClipboardChangedMessage [formats=" + formats + ", sendTime=" + sendTime + ", messageSourceId="
				+ messageSourceId + "]";
	}

	// For Jackson (de)serialization
	protected void setFormats(Set<ClipboardFormat> formats) {
		this.formats = formats;
	}
}
