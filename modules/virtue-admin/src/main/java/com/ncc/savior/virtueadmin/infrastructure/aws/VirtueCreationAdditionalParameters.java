package com.ncc.savior.virtueadmin.infrastructure.aws;

/**
 * POJO class that handles extra information for a Virtue. This class is for
 * transient data used in virtue setup.
 */
public class VirtueCreationAdditionalParameters {

	private String subnetId;
	private String name;
	private String securityGroupId;

	public VirtueCreationAdditionalParameters(String name) {
		this.name = name;
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecurityGroupId() {
		return securityGroupId;
	}

	public void setSecurityGroupId(String securityGroupId) {
		this.securityGroupId = securityGroupId;
	}

}
