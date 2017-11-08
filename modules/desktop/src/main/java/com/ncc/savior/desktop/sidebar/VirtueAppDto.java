package com.ncc.savior.desktop.sidebar;

import java.net.URI;

public class VirtueAppDto {
	private String name;
	private URI iconUri;
	private String iconLocation;

	public VirtueAppDto(String name, URI iconUri) {
		super();
		this.name = name;
		this.iconUri = iconUri;
	}

	public VirtueAppDto(String name, String iconLocation) {
		this.name = name;
		this.iconLocation = iconLocation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URI getIconUri() {
		return iconUri;
	}

	public void setIconUri(URI iconUri) {
		this.iconUri = iconUri;
	}

	public String getIconLocation() {
		return iconLocation;
	}

	public void setIconLocation(String iconLocation) {
		this.iconLocation = iconLocation;
	}

	@Override
	public String toString() {
		return "VirtueAppDto [name=" + name + ", iconUri=" + iconUri + ", iconLocation=" + iconLocation + "]";
	}
}
