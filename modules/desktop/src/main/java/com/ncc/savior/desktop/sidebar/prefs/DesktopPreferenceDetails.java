package com.ncc.savior.desktop.sidebar.prefs;

/**
 * More User friendly description and details for a given preference. Does not
 * contain values or require querying actual preference backing store to
 * retrieve this data.
 *
 *
 */
public class DesktopPreferenceDetails {
	private String root;
	private boolean nodeCollection;
	private String description;
	private String name;

	public DesktopPreferenceDetails(String name, String description, String root, boolean nodeCollection) {
		super();
		this.root = root;
		this.nodeCollection = nodeCollection;
		this.description = description;
		this.name = name;
	}

	public DesktopPreferenceDetails(String name, String description, String root) {
		this(name, description, root, false);
	}

	public String getRoot() {
		return root;
	}

	public boolean isNodeCollection() {
		return nodeCollection;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

}
