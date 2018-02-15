package com.ncc.savior.virtueadmin.data.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.UserName;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

@Repository
public class SpringJpaTemplateManager implements ITemplateManager {
	@Autowired
	private VirtueTemplateRepository vtRepository;
	@Autowired
	private ApplicationDefinitionRepository appRepository;
	@Autowired
	private VirtualMachineTemplateRepository vmtRepository;
	@Autowired
	private UserNameRepository userRepo;

	public SpringJpaTemplateManager(VirtueTemplateRepository vtRepository,
			VirtualMachineTemplateRepository vmtRepository, ApplicationDefinitionRepository appRepository,
			UserNameRepository userRep) {
		this.vtRepository = vtRepository;
		this.vmtRepository = vmtRepository;
		this.appRepository = appRepository;
		this.userRepo = userRep;
	}

	@Override
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(User user) {
		Collection<VirtueTemplate> templates = vtRepository.findByUserNames(new UserName(user.getUsername()));
		Map<String, VirtueTemplate> ret = new HashMap<String, VirtueTemplate>();
		for (VirtueTemplate t : templates) {
			ret.put(t.getId(), t);
		}
		return ret;
	}

	@Override
	public Collection<String> getVirtueTemplateIdsForUser(User user) {
		Collection<VirtueTemplate> templates = vtRepository.findByUserNames(new UserName(user.getUsername()));
		Collection<String> ret = new HashSet<String>();
		for (VirtueTemplate t : templates) {
			ret.add(t.getId());
		}
		return ret;
	}

	public VirtueTemplate getVirtueTemplateForUser(User user, String templateId) {
		return vtRepository.findByUserNamesAndId(new UserName(user.getUsername()), templateId);
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
	public Iterable<ApplicationDefinition> getAllApplications() {
		return appRepository.findAll();
	}

	@Override
	public Optional<ApplicationDefinition> getApplicationDefinition(String applicationId) {
		return appRepository.findById(applicationId);
	}

	// @Override
	// public ApplicationDefinition getApplicationDefinition(String applicationId) {
	// return appRepository.findOne(applicationId);
	// }

	@Override
	public Optional<VirtualMachineTemplate> getVmTemplate(String templateId) {
		return vmtRepository.findById(templateId);
	}

	@Override
	public Optional<VirtueTemplate> getVirtueTemplate(String templateId) {
		return vtRepository.findById(templateId);
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		appRepository.save(app);
	}

	@Override
	public void addVmTemplate(VirtualMachineTemplate vmTemplate) {
		Collection<ApplicationDefinition> apps = vmTemplate.getApplications();
		vmTemplate.setApplications(new HashSet<ApplicationDefinition>());
		// Adding empty template and then adding applications (that are already in db)
		// seems to work better for jpa
		vmTemplate = vmtRepository.save(vmTemplate);
		for (ApplicationDefinition app : apps) {
			// assignApplicationToVmTemplate(vmTemplate.getId(), app.getId());
			Optional<ApplicationDefinition> manageredApp = appRepository.findById(app.getId());
			vmTemplate.getApplications().add(manageredApp.get());
		}
		vmtRepository.save(vmTemplate);
	}

	@Override
	public void addVirtueTemplate(VirtueTemplate template) {
		Collection<VirtualMachineTemplate> vms = template.getVmTemplates();
		template.setVmTemplates(new HashSet<VirtualMachineTemplate>());
		vtRepository.save(template);
		// adding empty template and then adding vmtempaltes (that are already in db)
		// seem to work better for jpa
		for (VirtualMachineTemplate vmt : vms) {
			assingVmTemplateToVirtueTemplate(template.getId(), vmt.getId());
			// VirtualMachineTemplate managedVmt = vmtRepository.findOne(vmt.getId());
			// template.getVmTemplates().add(managedVmt);
		}
		// vtRepository.save(template);
	}

	@Override
	public void assignVirtueTemplateToUser(User user, String virtueTemplateId) {
		UserName username = new UserName(user.getUsername());
		// TODO this seems inefficient, but it errors if the username does not exist.
		userRepo.save(username);
		VirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		vt.retrieveUserNames().add(username);
		vtRepository.save(vt);
	}

	@Override
	public void revokeVirtueTemplateFromUser(User user, String virtueTemplateId) {
		VirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		vt.retrieveUserNames().remove(new UserName(user.getUsername()));
		vtRepository.save(vt);
	}

	@Override
	public void assignApplicationToVmTemplate(String vmTemplateId, String applicationId) throws NoSuchElementException {
		VirtualMachineTemplate vmt = vmtRepository.findById(vmTemplateId).get();
		ApplicationDefinition app = appRepository.findById(applicationId).get();
		if (vmt != null && app != null) {
			vmt.getApplications().add(app);
			vmtRepository.save(vmt);
		}
	}

	@Override
	public void assingVmTemplateToVirtueTemplate(String virtueTemplateId, String vmTemplateId)
			throws NoSuchElementException {
		VirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		VirtualMachineTemplate vmt = vmtRepository.findById(vmTemplateId).get();
		vt.getVmTemplates().add(vmt);
		vtRepository.save(vt);
	}

	@Override
	public Collection<String> getUsers() {
		Collection<String> users = new ArrayList<String>();
		Iterator<UserName> itr = userRepo.findAll().iterator();
		while (itr.hasNext()) {
			UserName user = itr.next();
			users.add(user.getUsername());
		}
		return users;
	}

	@Override
	public void clear() {
		vtRepository.deleteAll();
		vmtRepository.deleteAll();
		appRepository.deleteAll();
		userRepo.deleteAll();
	}

	@Override
	public void deleteApplicationDefinition(String templateId) {
		appRepository.deleteById(templateId);
	}

	@Override
	public void deleteVmTemplate(String templateId) {
		vmtRepository.deleteById(templateId);
	}

	@Override
	public void deleteVirtueTemplate(String templateId) {
		vtRepository.deleteById(templateId);
	}

}
