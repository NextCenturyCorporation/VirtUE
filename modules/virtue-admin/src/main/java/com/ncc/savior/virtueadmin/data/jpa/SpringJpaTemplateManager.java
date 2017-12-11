package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.Map;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public class SpringJpaTemplateManager implements ITemplateManager {

	private VirtueTemplateRepository vtRepository;
	private ApplicationDefinitionRepository appRepository;
	private VirtualMachineTemplateRepository vmtRepository;

	public SpringJpaTemplateManager(VirtueTemplateRepository vtRepository,
			VirtualMachineTemplateRepository vmtRepository, ApplicationDefinitionRepository appRepository) {
		this.vtRepository = vtRepository;
		this.vmtRepository = vmtRepository;
		this.appRepository = appRepository;
	}

	@Override
	public Iterable<VirtueTemplate> getAllVirtueTemplates() {
		return vtRepository.findAll();

	}

	@Override
	public Iterable<VirtualMachineTemplate> getAllVirtualMachineTemplates() {
		return vmtRepository.findAll();
	}

	@Override
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getVirtueTemplateIdsForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApplicationDefinition getApplicationDefinition(String applicationId) {
		return appRepository.findOne(applicationId);
	}

	@Override
	public VirtueTemplate getTemplate(User user, String templateId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		appRepository.save(app);
	}

	@Override
	public void addVmTemplate(VirtualMachineTemplate vmTemplate) {
		vmtRepository.save(vmTemplate);
	}

	@Override
	public void addVirtueTemplate(VirtueTemplate template) {
		vtRepository.save(template);
	}

	@Override
	public void assignVirtueTemplateToUser(User user, String virtueTemplateId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void revokeVirtueTemplateFromUser(User user, String virtueTemplateId) {
		// TODO Auto-generated method stub

	}

}
