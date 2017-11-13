package com.ncc.savior.desktop.virtues;

import java.net.URI;

import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;

public class VirtueAppDto {
	private String name;
	private URI iconUri;
	private String iconLocation;
	private IConnectionParameters connectionParams;
	private String startCommand;

	public VirtueAppDto(String name, URI iconUri, String startCommand, IConnectionParameters conParams) {
		super();
		this.name = name;
		this.iconUri = iconUri;
		this.startCommand = startCommand;
		this.connectionParams = conParams;
	}

	public VirtueAppDto(String name, String iconLocation, String startCommand, IConnectionParameters conParams) {
		this.name = name;
		this.iconLocation = iconLocation;
		this.startCommand = startCommand;
		this.connectionParams = conParams;
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

	public IConnectionParameters getConnectionParams() {
		return connectionParams;
	}

	public void setConnectionParams(IConnectionParameters connectionParams) {
		this.connectionParams = connectionParams;
	}

	@Override
	public String toString() {
		return "VirtueAppDto [name=" + name + ", iconUri=" + iconUri + ", iconLocation=" + iconLocation
				+ ", connectionParams=" + connectionParams + "]";
	}

	public String getStartCommand() {
		return startCommand;
	}
}
