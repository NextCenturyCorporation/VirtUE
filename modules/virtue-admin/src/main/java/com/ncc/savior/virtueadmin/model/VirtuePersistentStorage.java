package com.ncc.savior.virtueadmin.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage.VirtuePersistentStorageId;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * POJO and JPA entity for persistent storage.
 *
 */
@Entity
@IdClass(VirtuePersistentStorageId.class)
public class VirtuePersistentStorage implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@Schema(description="the user who this storage is assigned to.")
	private String username;
	private String infrastructureId;
	@Id
	private String virtueTemplateId;

	public VirtuePersistentStorage(String username, String infrastructureId, String virtueTemplateId) {
		super();
		this.username = username;
		this.infrastructureId = infrastructureId;
		this.virtueTemplateId = virtueTemplateId;
	}

	protected VirtuePersistentStorage() {
	}

	public String getUsername() {
		return username;
	}

	public String getInfrastructureId() {
		return infrastructureId;
	}

	public String getVirtueTemplateId() {
		return virtueTemplateId;
	}

	@Override
	public String toString() {
		return "VirtuePersistentStorage [username=" + username + ", infrastructureId=" + infrastructureId
				+ ", virtueTemplateId=" + virtueTemplateId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((infrastructureId == null) ? 0 : infrastructureId.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((virtueTemplateId == null) ? 0 : virtueTemplateId.hashCode());
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
		VirtuePersistentStorage other = (VirtuePersistentStorage) obj;
		if (infrastructureId == null) {
			if (other.infrastructureId != null)
				return false;
		} else if (!infrastructureId.equals(other.infrastructureId))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (virtueTemplateId == null) {
			if (other.virtueTemplateId != null)
				return false;
		} else if (!virtueTemplateId.equals(other.virtueTemplateId))
			return false;
		return true;
	}

	public static class VirtuePersistentStorageId implements Serializable {
		private static final long serialVersionUID = 1L;
		String username;
		String virtueTemplateId;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getVirtueTemplateId() {
			return virtueTemplateId;
		}

		public void setVirtueTemplateId(String virtueTemplateId) {
			this.virtueTemplateId = virtueTemplateId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((username == null) ? 0 : username.hashCode());
			result = prime * result + ((virtueTemplateId == null) ? 0 : virtueTemplateId.hashCode());
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
			VirtuePersistentStorageId other = (VirtuePersistentStorageId) obj;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			if (virtueTemplateId == null) {
				if (other.virtueTemplateId != null)
					return false;
			} else if (!virtueTemplateId.equals(other.virtueTemplateId))
				return false;
			return true;
		}
	}
}
