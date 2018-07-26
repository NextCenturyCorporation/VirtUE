package com.ncc.savior.virtueadmin.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import com.ncc.savior.virtueadmin.model.ClipboardPermission.PermissionId;

@Entity
@IdClass(PermissionId.class)
// @Table(uniqueConstraints = { @UniqueConstraint(columnNames = {
// "sourceGroupId", "destinationGroupId" }) })
public class ClipboardPermission implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Not a real ID, but used to set a default for a source such that there is a
	 * permission for new virtues.
	 */
	public static final String DEFAULT_DESTINATION = "DEFAULT";
	// @Column(nullable = false)
	@Id
	private String sourceGroupId;
	// @Column(nullable = false)
	@Id
	private String destinationGroupId;
	private ClipboardPermissionOption permission;

	public ClipboardPermission(String sourceGroupId, String destinationGroupId, ClipboardPermissionOption permission) {
		super();
		this.sourceGroupId = sourceGroupId;
		this.destinationGroupId = destinationGroupId;
		this.permission = permission;
	}

	public ClipboardPermission() {

	}

	public String getSourceGroupId() {
		return sourceGroupId;
	}

	public void setSourceGroupId(String sourceGroupId) {
		this.sourceGroupId = sourceGroupId;
	}

	public String getDestinationGroupId() {
		return destinationGroupId;
	}

	public void setDestinationGroupId(String destinationGroupId) {
		this.destinationGroupId = destinationGroupId;
	}

	public ClipboardPermissionOption getPermission() {
		return permission;
	}

	public void setPermission(ClipboardPermissionOption permission) {
		this.permission = permission;
	}

	public static class PermissionId implements Serializable {
		private static final long serialVersionUID = 1L;
		String sourceGroupId;
		String destinationGroupId;
	}
}
