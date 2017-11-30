package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public interface ITemplateManager {
	Collection<VirtueTemplate> getAllVirtueTemplates();

	Collection<VirtualMachineTemplate> getAllVirtualMachineTemplates();

	Map<String, VirtueTemplate> getVirtueTemplatesForUser(User user);

	Collection<String> getVirtueTemplateIdsForUser(User user);

	ApplicationDefinition getApplicationDefinition(String applicationId);

	VirtueTemplate getTemplate(User user, String templateId);

	void addApplicationDefinition(ApplicationDefinition app);

	void addVmTemplate(VirtualMachineTemplate vmTemplate);

	void addVirtueTemplate(VirtueTemplate template);

	void assignVirtueTemplateToUser(User user, String virtueTemplateId);
}
