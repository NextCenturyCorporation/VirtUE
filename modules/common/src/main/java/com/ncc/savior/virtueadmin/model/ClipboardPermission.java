package com.ncc.savior.virtueadmin.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import com.ncc.savior.virtueadmin.model.ClipboardPermission.PermissionId;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Permission data transfer object (DTO) for clipboard permissions. Each
 * permission only contains a source and destination as well as a value as
 * {@link ClipboardPermissionOption}. The value designates whether and what
 * actions are required by the user for clipboard data to be passed from the
 * specified source to the specified destination.
 */
@Entity
@IdClass(PermissionId.class)
// @Table(uniqueConstraints = { @UniqueConstraint(columnNames = {
// "sourceGroupId", "destinationGroupId" }) })
@Schema(description="Controls permissions for cross virtue clipboard actions.")
public class ClipboardPermission implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Not a real ID, but used to set a default for a source such that there is a
	 * permission for new virtues.
	 */
	public static final String DEFAULT_DESTINATION = "DEFAULT";
	public static final String DESKTOP_CLIENT_GROUP_ID = "Desktop-client";
	// @Column(nullable = false)
	@Id
	@Schema(description="ID of the source of the clipboard message.  In Savior, this is the virtue template ID.")
	private String sourceGroupId;
	// @Column(nullable = false)
	@Id
	@Schema(description="ID of the destination of the clipboard message.  In Savior, this is the virtue template ID.")
	private String destinationGroupId;
	@Schema(description="The actual permission option for the given source and destination of a clipboard message.")
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((destinationGroupId == null) ? 0 : destinationGroupId.hashCode());
			result = prime * result + ((sourceGroupId == null) ? 0 : sourceGroupId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PermissionId other = (PermissionId) obj;
			if (destinationGroupId == null) {
				if (other.destinationGroupId != null)
					return false;
			} else if (!destinationGroupId.equals(other.destinationGroupId))
				return false;
			if (sourceGroupId == null) {
				if (other.sourceGroupId != null)
					return false;
			} else if (!sourceGroupId.equals(other.sourceGroupId))
				return false;
			return true;
		}
	}
}
