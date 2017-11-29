package com.ncc.savior.desktop.virtues;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ncc.savior.virtueadmin.model.VirtueState;

/**
 * Data Transfer Object (DTO) for a virtue.
 *
 *
 */
public class VirtueDto {
	private String name;
	private List<VirtueAppDto> apps;
	private String id;
	private VirtueState status;
	private String templateId;

	public VirtueDto(String id, String name, String templateId, List<VirtueAppDto> apps) {
		super();
		this.id = id;
		this.name = name;
		this.apps = apps;
		this.templateId = templateId;
		this.status = null;
	}

	public VirtueDto(String id, String name, String templateId, VirtueAppDto... virtueAppDtos) {
		super();
		this.id = id;
		this.name = name;
		this.apps = new ArrayList<VirtueAppDto>(virtueAppDtos.length);
		this.templateId = templateId;
		this.status = null;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public VirtueState getStatus() {
		return status;
	}

	public void setStatus(VirtueState status) {
		this.status = status;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	@Override
	public String toString() {
		return "VirtueDto [name=" + name + ", apps=" + apps + ", id=" + id + ", status=" + status + ", templateId="
				+ templateId + "]";
	}

	public static class VirtueDtoComparator implements Comparator<VirtueDto> {

		@Override
		public int compare(VirtueDto o1, VirtueDto o2) {
			int compare = o1.getId().compareTo(o2.getId());
			if (compare == 0) {
				compare = o1.getName().compareTo(o2.getName());
			}
			return compare;
		}

	}
}
