package com.ncc.savior.virtueadmin.model;

import java.util.List;

public class CifsShareCreationParameter {
	private String name;
	private String virtueId;
	private String server;
	private String path;
	private List<String> permissions;
	private String type;
	private String exportedName;
	
	public CifsShareCreationParameter() {
	}
	
	public CifsShareCreationParameter(String name, String virtueId, String server, String path,
			List<String> permissions, String type) {
		super();
		this.name = name;
		this.virtueId = virtueId;
		this.server = server;
		this.path = path;
		this.permissions = permissions;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVirtueId() {
		return virtueId;
	}
	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getExportedName() {
		return exportedName;
	}
	public void setExportedName(String exportedName) {
		this.exportedName = exportedName;
	}

	@Override
	public String toString() {
		return "CifsShareCreationParameter [name=" + name + ", virtueId=" + virtueId + ", server=" + server + ", path="
				+ path + ", permissions=" + permissions + ", type=" + type + ", exportedName=" + exportedName + "]";
	}
	
}
