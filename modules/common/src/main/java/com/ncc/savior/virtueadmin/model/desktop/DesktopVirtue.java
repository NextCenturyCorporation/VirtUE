package com.ncc.savior.virtueadmin.model.desktop;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class DesktopVirtue {
	private String id;
	private String name;
	private String templateId;
	private Map<String, ApplicationDefinition> apps;

	public DesktopVirtue(String id, String name, String templateId, Map<String, ApplicationDefinition> apps) {
		super();
		this.name = name;
		this.apps = apps;
		this.id = id;
		this.templateId = templateId;
	}

	public DesktopVirtue(String id, String name, String templateId) {
		super();
		this.name = name;
		this.apps = new HashMap<String, ApplicationDefinition>();
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

	public Map<String, ApplicationDefinition> getApps() {
		return apps;
	}

	public void setApps(Map<String, ApplicationDefinition> apps) {
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
			if (o1.getId() == null) {
				if (o2.getId() == null) {
					return o1.getTemplateId().compareTo(o2.getTemplateId());
				} else {
					return -1;
				}
			}
			if (o2.getId() == null) {
				return 1;
			}
			int compare = o1.getId().compareTo(o2.getId());
			if (compare == 0) {
				compare = o1.getName().compareTo(o2.getName());
			}
			return compare;
		}

	}
}
