package com.ncc.savior.virtueadmin.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Describes the status of the virtual machine.")
public enum VmState {
	CREATING, STOPPED, LAUNCHING, RUNNING, PAUSING, PAUSED, RESUMING, STOPPING, DELETING, ERROR, DELETED
}
