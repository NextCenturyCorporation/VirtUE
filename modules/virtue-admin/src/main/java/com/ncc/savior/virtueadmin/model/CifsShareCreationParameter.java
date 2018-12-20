package com.ncc.savior.virtueadmin.model;

public class CifsShareCreationParameter {
	private String name;
	private String id;
	private String username;
	private String password;

	protected CifsShareCreationParameter() {

	}

	public CifsShareCreationParameter(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public CifsShareCreationParameter(String name, String id, String username, String password) {
		super();
		this.name = name;
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "CifsShareCreationParameter [name=" + name + ", id=" + id + ", username=" + username + ", password="
				+ password + "]";
	}
}
