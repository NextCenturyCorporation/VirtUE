package com.ncc.savior.desktop.alerting;

import com.ncc.savior.virtueadmin.model.VirtueInstance;

/**
 * Alert message specific to a virtue. We may want to have additional ways to
 * handle these alerts.
 */
public class VirtueAlertMessage extends BaseAlertMessage {
	private String virtueId;
	private String virtueName;
	private String message;

	public VirtueAlertMessage(String title, VirtueInstance virtue, String message) {
		this(title, virtue.getId(), virtue.getName(), message);

	}

	public VirtueAlertMessage(String title, String id, String name, String message) {
		super(title);
		this.virtueId = id;
		this.virtueName = name;
		this.message = message;
	}

	@Override
	public String getPlainTextMessage() {
		return message;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public String getVirtueName() {
		return virtueName;
	}
}
