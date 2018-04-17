package com.ncc.savior.virtueadmin.infrastructure.statemachine;

public enum ProvisionStates {
	UNPROVISIONED, XEN_INITIATED, XEN_NETWORKING_OK, XEN_READY, XEN_STARTING_VMS, XEN_VMS_STARTED, ALL_STARTED
}
