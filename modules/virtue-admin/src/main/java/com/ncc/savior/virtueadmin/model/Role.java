package com.ncc.savior.virtueadmin.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object (DTO) for roles.
 * 
 *
 */
public class Role {
	private String id;
	private String name;
	private String version;
	private Set<String> applicationIds;
	private Set<String> startingResourceIds;
	private Set<String> startingTransducerIds;

	public Role(String id, String name, String version, Set<String> applicationIds, Set<String> startingResourceIds,
			Set<String> startingTransducerIds) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.applicationIds = applicationIds;
		this.startingResourceIds = startingResourceIds;
		this.startingTransducerIds = startingTransducerIds;
	}

	public Role(String id, String name, String version, Set<String> applicationIds) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.applicationIds = applicationIds;
		this.startingResourceIds = new HashSet<String>();
		this.startingTransducerIds = new HashSet<String>();
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

	public Set<String> getApplicationIds() {
		return applicationIds;
	}

	public Set<String> getStartingResourceIds() {
		return startingResourceIds;
	}

	public Set<String> getStartingTransducerIds() {
		return startingTransducerIds;
	}

}
