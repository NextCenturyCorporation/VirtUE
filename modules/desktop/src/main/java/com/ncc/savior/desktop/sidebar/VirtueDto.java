package com.ncc.savior.desktop.sidebar;

import java.util.ArrayList;
import java.util.List;

public class VirtueDto {
	private String name;
	private List<VirtueAppDto> apps;

	public VirtueDto(String name, List<VirtueAppDto> apps) {
		super();
		this.name = name;
		this.apps = apps;
	}

	public VirtueDto(String name, VirtueAppDto... virtueAppDtos) {
		super();
		this.name = name;
		this.apps = new ArrayList<VirtueAppDto>(virtueAppDtos.length);
		for (VirtueAppDto dto : virtueAppDtos) {
			this.apps.add(dto);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<VirtueAppDto> getApps() {
		return apps;
	}

	public void setApps(List<VirtueAppDto> apps) {
		this.apps = apps;
	}

	@Override
	public String toString() {
		return "VirtueDto [name=" + name + ", apps=" + apps + "]";
	}
}
