package com.ncc.savior.virtueadmin.infrastructure.aws;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;

/**
 * POJO class that handles extra information for a Virtue. This class is for
 * transient data used in virtue setup.
 */
public class VirtueCreationAdditionalParameters {

	private String subnetId;
	private String name;
	private String securityGroupId;
	private String virtueId;
	private String virtueTemplateId;
	private VirtuePrimaryPurpose primaryPurpose;
	private VirtueSecondaryPurpose secondaryPurpose;

	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}

	public void setVirtueTemplateId(String virtueTemplateId) {
		this.virtueTemplateId = virtueTemplateId;
	}

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

	public String getVirtueId() {
		return virtueId;
	}

	public String getVirtueTemplateId() {
		return virtueTemplateId;
	}

	public VirtuePrimaryPurpose getPrimaryPurpose() {
		return primaryPurpose;
	}

	public void setPrimaryPurpose(VirtuePrimaryPurpose primaryPurpose) {
		this.primaryPurpose = primaryPurpose;
	}

	public VirtueSecondaryPurpose getSecondaryPurpose() {
		return secondaryPurpose;
	}

	public void setSecondaryPurpose(VirtueSecondaryPurpose secondaryPurpose) {
		this.secondaryPurpose = secondaryPurpose;
	}
}
