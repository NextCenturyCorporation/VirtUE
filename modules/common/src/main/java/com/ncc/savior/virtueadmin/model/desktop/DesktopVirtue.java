package com.ncc.savior.virtueadmin.model.desktop;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;

public class DesktopVirtue {
	private String id;
	private String name;
	private String templateId;
	private Map<String, ApplicationDefinition> apps;
	private VirtueState virtueState;

	public DesktopVirtue(String id, String name, String templateId, Map<String, ApplicationDefinition> apps,
			VirtueState virtueState) {
		super();
		this.name = name;
		this.apps = apps;
		this.id = id;
		this.templateId = templateId;
		this.virtueState = virtueState;
	}

	public DesktopVirtue(String id, String name, String templateId, VirtueState virtueState) {
		super();
		this.name = name;
		this.apps = new HashMap<String, ApplicationDefinition>();
		this.id = id;
		this.templateId = templateId;
		this.virtueState = virtueState;
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
		return "DesktopVirtue [id=" + id + ", name=" + name + ", templateId=" + templateId + ", apps=" + apps
				+ ", virtueState=" + virtueState + "]";
	}

	public VirtueState getVirtueState() {
		return virtueState;
	}

	public void setVirtueState(VirtueState virtueState) {
		this.virtueState = virtueState;
	}

	public static class DesktopVirtueComparator implements Comparator<DesktopVirtue> {

		@Override
		public int compare(DesktopVirtue o1, DesktopVirtue o2) {
			int tempComp = String.CASE_INSENSITIVE_ORDER.compare(o1.getTemplateId(), o2.getTemplateId());
			if (tempComp != 0) {
				return tempComp;
			} else {
				String id1 = o1.getId();
				String id2 = o2.getId();
				if (id1 == null) {
					if (id2 == null) {
						return 0;
					} else {
						return 1;
					}
				}
				if (id2 == null) {
					return -1;
				}
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId());
			}
		}
	}
}
