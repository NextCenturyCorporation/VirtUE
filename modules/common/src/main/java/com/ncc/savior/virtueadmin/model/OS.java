package com.ncc.savior.virtueadmin.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Descriptor for which Operating System is being used for a virtual machine or application.")
public enum OS {
	LINUX, WINDOWS, MAC
}
