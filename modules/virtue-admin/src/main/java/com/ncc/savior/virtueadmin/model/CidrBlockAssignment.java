package com.ncc.savior.virtueadmin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Database model Entity for storing the assignment of a cidr block to a subnet.
 * 
 *
 */
@Entity
public class CidrBlockAssignment {
	@Column(unique = true)
	private String cidrBlock;
	@Id
	private String assignmentId;
	private String username;
	private String infrastructureId;

	/**
	 * Used for jackson deserialization
	 */
	protected CidrBlockAssignment() {

	}

	public CidrBlockAssignment(String cidrBlock, String assignmentId, String username, String infrastructureId) {
		super();
		this.cidrBlock = cidrBlock;
		this.assignmentId = assignmentId;
		this.username = username;
		this.infrastructureId = infrastructureId;
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

	public String getInfrastructureId() {
		return infrastructureId;
	}

	public void setInfrastructureId(String infrastructureId) {
		this.infrastructureId = infrastructureId;
	}

	@Override
	public String toString() {
		return "CidrBlockAssignment [cidrBlock=" + cidrBlock + ", assignmentId=" + assignmentId + ", username="
				+ username + ", infrastructurId=" + infrastructureId + "]";
	}

}
