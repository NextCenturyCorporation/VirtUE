/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
	private String color;

	public DesktopVirtue(String id, String name, String templateId, Map<String, ApplicationDefinition> apps,
			VirtueState virtueState, String color) {
		super();
		this.name = name;
		this.apps = apps;
		this.id = id;
		this.templateId = templateId;
		this.virtueState = virtueState;
		this.color=color;
	}

	public DesktopVirtue(String id, String name, String templateId) {
		super();
		this.name = name;
		this.apps = new HashMap<String, ApplicationDefinition>();
		this.id = id;
		this.templateId = templateId;
		this.virtueState = VirtueState.UNPROVISIONED;
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

	public VirtueState getVirtueState() {
		return virtueState;
	}

	public void setVirtueState(VirtueState virtueState) {
		this.virtueState = virtueState;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public String toString() {
		return "DesktopVirtue [id=" + id + ", name=" + name + ", templateId=" + templateId + ", apps=" + apps
				+ ", virtueState=" + virtueState + ", color=" + color + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apps == null) ? 0 : apps.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((templateId == null) ? 0 : templateId.hashCode());
		result = prime * result + ((virtueState == null) ? 0 : virtueState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DesktopVirtue other = (DesktopVirtue) obj;
		if (apps == null) {
			if (other.apps != null)
				return false;
		} else if (!apps.equals(other.apps))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (templateId == null) {
			if (other.templateId != null)
				return false;
		} else if (!templateId.equals(other.templateId))
			return false;
		if (virtueState != other.virtueState)
			return false;
		return true;
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
