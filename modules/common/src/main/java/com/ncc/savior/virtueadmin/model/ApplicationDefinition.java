package com.ncc.savior.virtueadmin.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Application Data Transfer Object (DTO).
 * 
 *
 */
@Entity
public class ApplicationDefinition {
	@Id
	private String id;
	private String name;
	private String version;
	private OS os;
	private String launchCommand;
	private String iconKey;

	public ApplicationDefinition(String id, String name, String version, OS os, String iconKey) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
		this.iconKey = iconKey;
	}

	public ApplicationDefinition(String id, String displayName, String version, OS os, String iconKey,
			String launchCommand) {
		this.id = id;
		this.name = displayName;
		this.version = version;
		this.os = os;
		this.launchCommand = launchCommand;
		this.iconKey = iconKey;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected ApplicationDefinition() {

	}

	public ApplicationDefinition(String templateId, ApplicationDefinition appDef) {
		this.id = templateId;
		this.name = appDef.getName();
		this.version = appDef.getVersion();
		this.os = appDef.getOs();
		this.launchCommand = appDef.getLaunchCommand();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public OS getOs() {
		return os;
	}

	public String getLaunchCommand() {
		return launchCommand;
	}

	// below setters used for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setOs(OS os) {
		this.os = os;
	}

	public void setLaunchCommand(String launchCommand) {
		this.launchCommand = launchCommand;
	}

	public String getIconKey() {
		return iconKey;
	}

	public void setIconKey(String iconKey) {
		this.iconKey = iconKey;
	}

	@Override
	public String toString() {
		return "ApplicationDefinition [id=" + id + ", name=" + name + ", version=" + version + ", os=" + os
				+ ", launchCommand=" + launchCommand + ", iconKey=" + iconKey + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iconKey == null) ? 0 : iconKey.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((launchCommand == null) ? 0 : launchCommand.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((os == null) ? 0 : os.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		ApplicationDefinition other = (ApplicationDefinition) obj;
		if (iconKey == null) {
			if (other.iconKey != null)
				return false;
		} else if (!iconKey.equals(other.iconKey))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (launchCommand == null) {
			if (other.launchCommand != null)
				return false;
		} else if (!launchCommand.equals(other.launchCommand))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (os != other.os)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
