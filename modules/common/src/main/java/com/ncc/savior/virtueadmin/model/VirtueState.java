package com.ncc.savior.virtueadmin.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Describes the status of the Virtue.")
public enum VirtueState {
	
	ERROR(10), RUNNING(20), RESUMING(30), LAUNCHING(40), CREATING(50), PAUSED(60), PAUSING(70), STOPPING(80), 
	STOPPED(90), DELETING(100), DELETED(110), UNPROVISIONED(120);
	
	VirtueState(int value) {
		this.value = value;
	}

	private final int value;

	public int getValue() {
		return value;
	}
}
