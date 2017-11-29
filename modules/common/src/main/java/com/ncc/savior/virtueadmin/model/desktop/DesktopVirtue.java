package com.ncc.savior.virtueadmin.model.desktop;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DesktopVirtue {
	private String id;
	private String name;
	private String templateId;
	private Map<String, DesktopVirtueApplication> apps;

	public DesktopVirtue(String id, String name, String templateId, Map<String, DesktopVirtueApplication> apps) {
		super();
		this.name = name;
		this.apps = apps;
		this.id = id;
		this.templateId = templateId;
	}

	public DesktopVirtue(String id, String name, String templateId) {
		super();
		this.name = name;
		this.apps = new HashMap<String, DesktopVirtueApplication>();
		this.id = id;
		this.templateId = templateId;
	}

	protected DesktopVirtue() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, DesktopVirtueApplication> getApps() {
		return apps;
	}

	public void setApps(Map<String, DesktopVirtueApplication> apps) {
		this.apps = apps;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	@Override
	public String toString() {
		return "DesktopVirtue [id=" + id + ", name=" + name + ", templateId=" + templateId + ", apps=" + apps + "]";
	}

	public static class DesktopVirtueComparator implements Comparator<DesktopVirtue> {

		@Override
		public int compare(DesktopVirtue o1, DesktopVirtue o2) {
			int compare = o1.getId().compareTo(o2.getId());
			if (compare == 0) {
				compare = o1.getName().compareTo(o2.getName());
			}
			return compare;
		}

	}
}
