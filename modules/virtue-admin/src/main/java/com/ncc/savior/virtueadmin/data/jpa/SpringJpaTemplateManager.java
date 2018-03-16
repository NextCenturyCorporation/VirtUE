package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.util.SaviorException;

import persistance.JpaVirtualMachineTemplate;
import persistance.JpaVirtueTemplate;
import persistance.JpaVirtueUser;

/**
 * {@link ITemplateManager} that uses Spring and JPA.
 */
@Repository
public class SpringJpaTemplateManager implements ITemplateManager {
	@Autowired
	private VirtueTemplateRepository vtRepository;
	@Autowired
	private ApplicationDefinitionRepository appRepository;
	@Autowired
	private VirtualMachineTemplateRepository vmtRepository;
	@Autowired
	private UserRepository userRepo;

	public SpringJpaTemplateManager(VirtueTemplateRepository vtRepository,
			VirtualMachineTemplateRepository vmtRepository, ApplicationDefinitionRepository appRepository,
			UserRepository userRep) {
		this.vtRepository = vtRepository;
		this.vmtRepository = vmtRepository;
		this.appRepository = appRepository;
		this.userRepo = userRep;
	}

	@Override
	public Map<String, JpaVirtueTemplate> getVirtueTemplatesForUser(JpaVirtueUser user) {
		user = userRepo.findById(user.getUsername()).orElse(null);
		if (user == null) {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + user + " not found.");
		}
		Collection<JpaVirtueTemplate> templates = user.getVirtueTemplates();
		Map<String, JpaVirtueTemplate> ret = new HashMap<String, JpaVirtueTemplate>();
		for (JpaVirtueTemplate t : templates) {
			ret.put(t.getId(), t);
		}
		return ret;
	}

	@Override
	public Collection<String> getVirtueTemplateIdsForUser(JpaVirtueUser user) {
		user = userRepo.findById(user.getUsername()).orElse(null);
		if (user == null) {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + user + " not found.");
		}
		Collection<JpaVirtueTemplate> templates = user.getVirtueTemplates();
		Collection<String> ret = new HashSet<String>();
		for (JpaVirtueTemplate t : templates) {
			ret.add(t.getId());
		}
		return ret;
	}

	@Override
	public JpaVirtueTemplate getVirtueTemplateForUser(JpaVirtueUser user, String templateId) {
		user = userRepo.findById(user.getUsername()).orElse(null);
		if (user == null) {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + user + " not found.");
		}
		for (JpaVirtueTemplate template : user.getVirtueTemplates()) {
			if (template.getId().equals(templateId)) {
				return template;
			}
		}
		throw new SaviorException(SaviorException.VIRTUE_TEMPLATE_ID_NOT_FOUND,
				"Virtue Template id=" + templateId + " not found.");
	}

	@Override
	public Iterable<JpaVirtueTemplate> getAllVirtueTemplates() {
		return vtRepository.findAll();

	}

	@Override
	public Iterable<JpaVirtualMachineTemplate> getAllVirtualMachineTemplates() {
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
	public Optional<JpaVirtualMachineTemplate> getVmTemplate(String templateId) {
		return vmtRepository.findById(templateId);
	}

	@Override
	public Optional<JpaVirtueTemplate> getVirtueTemplate(String templateId) {
		return vtRepository.findById(templateId);
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		appRepository.save(app);
	}

	@Override
	public void addVmTemplate(JpaVirtualMachineTemplate vmTemplate) {
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
	public void addVirtueTemplate(JpaVirtueTemplate template) {
		Collection<JpaVirtualMachineTemplate> vms = template.getVmTemplates();
		template.setVmTemplates(new HashSet<JpaVirtualMachineTemplate>());
		vtRepository.save(template);
		// adding empty template and then adding vmtempaltes (that are already in db)
		// seem to work better for jpa
		for (JpaVirtualMachineTemplate vmt : vms) {
			assingVmTemplateToVirtueTemplate(template.getId(), vmt.getId());
			// VirtualMachineTemplate managedVmt = vmtRepository.findOne(vmt.getId());
			// template.getVmTemplates().add(managedVmt);
		}
		// vtRepository.save(template);
	}

	@Override
	public void assignVirtueTemplateToUser(JpaVirtueUser user, String virtueTemplateId) {
		// UserName username = new UserName(user.getUsername());
		// TODO this seems inefficient, but it errors if the username does not exist.
		JpaVirtueUser existing = userRepo.findById(user.getUsername()).orElse(null);
		if (existing != null) {
			user = existing;
		}
		JpaVirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		user.addVirtueTemplate(vt);
		userRepo.save(user);
	}

	@Override
	public void revokeVirtueTemplateFromUser(JpaVirtueUser user, String virtueTemplateId) {
		JpaVirtueUser existing = userRepo.findById(user.getUsername()).orElse(null);
		if (existing != null) {
			JpaVirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
			existing.removeVirtueTemplate(vt);
			userRepo.save(existing);
		}
	}

	@Override
	public void assignApplicationToVmTemplate(String vmTemplateId, String applicationId) throws NoSuchElementException {
		JpaVirtualMachineTemplate vmt = vmtRepository.findById(vmTemplateId).get();
		ApplicationDefinition app = appRepository.findById(applicationId).get();
		if (vmt != null && app != null) {
			vmt.getApplications().add(app);
			vmtRepository.save(vmt);
		}
	}

	@Override
	public void assingVmTemplateToVirtueTemplate(String virtueTemplateId, String vmTemplateId)
			throws NoSuchElementException {
		JpaVirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		JpaVirtualMachineTemplate vmt = vmtRepository.findById(vmTemplateId).get();
		vt.getVmTemplates().add(vmt);
		vtRepository.save(vt);
	}

	@Override
	public Collection<String> getUsersWithTemplate() {
		Iterable<JpaVirtueUser> allUsers = userRepo.findAll();
		Iterator<JpaVirtueUser> itr = allUsers.iterator();
		Set<String> users = new HashSet<String>();
		while (itr.hasNext()) {
			JpaVirtueUser user = itr.next();
			if (!user.getVirtueTemplates().isEmpty()) {
				users.add(user.getUsername());
			}
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

	@Override
	public void test() {
		Iterable<JpaVirtualMachineTemplate> t = vmtRepository.findAll();
		Iterator<JpaVirtualMachineTemplate> itr = t.iterator();
		itr.next();

		JpaVirtualMachineTemplate vmt = itr.next();

		Collection<ApplicationDefinition> app = vmt.getApplications();
		String s = app.toString();
		System.out.println(s);
	}

	@Override
	public Iterable<JpaVirtualMachineTemplate> getVmTemplatesById(Collection<String> vmTemplateIds) {
		return vmtRepository.findAllById(vmTemplateIds);
	}

	@Override
	public Iterable<ApplicationDefinition> getApplicationDefinitions(Collection<String> applicationIds) {
		return appRepository.findAllById(applicationIds);
	}

}
