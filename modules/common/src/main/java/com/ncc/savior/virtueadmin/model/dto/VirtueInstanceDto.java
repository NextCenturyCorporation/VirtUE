package com.ncc.savior.virtueadmin.model.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.BaseVirtueInstance;
import com.ncc.savior.virtueadmin.model.BaseVirtueTemplate;

/**
 * Virtue class models a virtual unit with the user, applications etc.
 * 
 * 
 */
public class VirtueInstanceDto extends BaseVirtueInstance {
	private Collection<String> vmIds;
	// private Set<String> transducers;

	public VirtueInstanceDto(String id, String name, String username, String templateId, Collection<String> vmIds) {
		super(id, name, username, templateId);
		this.vmIds = vmIds;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected VirtueInstanceDto() {

	}

	public VirtueInstanceDto(VirtueTemplateDto template, String username) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				new HashSet<String>());

	}

	public VirtueInstanceDto(BaseVirtueTemplate template, String username, Collection<String> vmIds) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				vmIds);

	}

	public VirtueInstanceDto(BaseVirtueInstance template, String username, Collection<String> vmIds) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(), vmIds);

	}

	public Collection<String> getVmIds() {
		return vmIds;
	}

	public void setVmIds(Collection<String> vmIds) {
		this.vmIds = vmIds;
	}

	@Override
	public String toString() {
		return "RestVirtueInstance [vmIds=" + vmIds + ", id=" + id + ", name=" + name + ", username=" + username
				+ ", templateId=" + templateId + ", state=" + state + "]";
	}
}
