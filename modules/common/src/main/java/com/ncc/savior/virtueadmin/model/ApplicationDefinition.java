package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

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
	@Transient
	private String parameters;
	@ElementCollection
	private List<String> tags;

	public ApplicationDefinition(String id, String name, String version, OS os, String iconKey, List<String> tags) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
		this.iconKey = iconKey;
		if (tags == null) {
			tags = new ArrayList<String>(0);
		}
		this.tags = tags;
	}

	public ApplicationDefinition(String id, String displayName, String version, OS os, String iconKey,
			String launchCommand, List<String> tags) {
		this.id = id;
		this.name = displayName;
		this.version = version;
		this.os = os;
		this.launchCommand = launchCommand;
		this.iconKey = iconKey;
		if (tags == null) {
			tags = new ArrayList<String>(0);
		}
		this.tags = tags;
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

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "ApplicationDefinition [id=" + id + ", name=" + name + ", version=" + version + ", os=" + os
				+ ", launchCommand=" + launchCommand + ", iconKey=" + iconKey + ", parameters=" + parameters + ", tags="
				+ tags + "]";
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
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass()) {
			return false;
		}
		ApplicationDefinition other = (ApplicationDefinition) obj;

		// check if both null or same reference, and if not, then check equals.
		if (iconKey != other.iconKey || !iconKey.equals(other.iconKey)) {
			return false;
		}
		if (id != other.id || !id.equals(other.id)) {
			return false;
		}
		if (launchCommand != other.launchCommand || !launchCommand.equals(other.launchCommand)) {
			return false;
		}
		if (name != other.name || !name.equals(other.name)) {
			return false;
		}
		if (os != other.os || !os.equals(other.os)) {
			return false;
		}
		if (tags != other.tags || !tags.equals(other.tags)) {
			return false;
		}
		if (version != other.version || !version.equals(other.version)) {
			return false;
		}

		return true;
	}
	public static final Comparator<? super ApplicationDefinition> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	private static class CaseInsensitiveNameComparator implements Comparator<ApplicationDefinition> {
		@Override
		public int compare(ApplicationDefinition o1, ApplicationDefinition o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
		}
	}
}
