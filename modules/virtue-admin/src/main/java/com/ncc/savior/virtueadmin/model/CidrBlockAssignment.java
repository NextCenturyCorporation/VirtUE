package com.ncc.savior.virtueadmin.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CidrBlockAssignment {
	@Id
	private String cidrBlock;
	private String assignmentId;
	private String username;
	private String infrastructurId;

	/**
	 * Used for jackson deserialization
	 */
	protected CidrBlockAssignment() {

	}
	
	public CidrBlockAssignment(String cidrBlock, String assignmentId, String username, String infrastructurId) {
		super();
		this.cidrBlock = cidrBlock;
		this.assignmentId = assignmentId;
		this.username = username;
		this.infrastructurId = infrastructurId;
	}

	public String getCidrBlock() {
		return cidrBlock;
	}

	public void setCidrBlock(String cidrBlock) {
		this.cidrBlock = cidrBlock;
	}

	public String getAssignmentId() {
		return assignmentId;
	}

	public void setAssignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getInfrastructurId() {
		return infrastructurId;
	}

	public void setInfrastructurId(String infrastructurId) {
		this.infrastructurId = infrastructurId;
	}

}
