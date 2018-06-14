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

import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(VirtueUser user) {
		user = userRepo.findById(user.getUsername()).orElse(null);
		if (user == null) {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + user + " not found.");
		}
		Collection<VirtueTemplate> templates = user.getVirtueTemplates();
		Map<String, VirtueTemplate> ret = new HashMap<String, VirtueTemplate>();
		for (VirtueTemplate t : templates) {
			ret.put(t.getId(), t);
		}
		return ret;
	}

	@Override
	public Collection<String> getVirtueTemplateIdsForUser(VirtueUser user) {
		user = userRepo.findById(user.getUsername()).orElse(null);
		if (user == null) {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + user + " not found.");
		}
		Collection<VirtueTemplate> templates = user.getVirtueTemplates();
		Collection<String> ret = new HashSet<String>();
		for (VirtueTemplate t : templates) {
			ret.add(t.getId());
		}
		return ret;
	}

	@Override
	public VirtueTemplate getVirtueTemplateForUser(VirtueUser user, String templateId) {
		user = userRepo.findById(user.getUsername()).orElse(null);
		if (user == null) {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + user + " not found.");
		}
		for (VirtueTemplate template : user.getVirtueTemplates()) {
			if (template.getId().equals(templateId)) {
				return template;
			}
		}
		throw new SaviorException(SaviorException.VIRTUE_TEMPLATE_ID_NOT_FOUND,
				"Virtue Template id=" + templateId + " not found.");
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
	public ApplicationDefinition getApplicationDefinition(String applicationId) {
		Optional<ApplicationDefinition> oa = appRepository.findById(applicationId);
		if (oa.isPresent()) {
			return oa.get();
		} else {
			throw new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
					"Cannot find application with id=" + applicationId);
		}
	}

	// @Override
	// public ApplicationDefinition getApplicationDefinition(String applicationId) {
	// return appRepository.findOne(applicationId);
	// }

	@Override
	public VirtualMachineTemplate getVmTemplate(String templateId) {
		Optional<VirtualMachineTemplate> ovmt = vmtRepository.findById(templateId);
		if (ovmt.isPresent()) {
			return ovmt.get();
		} else {
			throw new SaviorException(SaviorException.VM_TEMPLATE_NOT_FOUND,
					"Cannot find VM template with id=" + templateId);
		}
	}

	@Override
	public VirtueTemplate getVirtueTemplate(String templateId) {
		Optional<VirtueTemplate> ovt = vtRepository.findById(templateId);
		if (ovt.isPresent()) {
			return ovt.get();
		} else {
			throw new SaviorException(SaviorException.VIRTUE_TEMPLATE_ID_NOT_FOUND,
					"Cannot find virtue template with id=" + templateId);
		}
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		appRepository.save(app);
	}

	@Override
	public void addVmTemplate(VirtualMachineTemplate vmTemplate) {
		Collection<ApplicationDefinition> apps = new HashSet<ApplicationDefinition>();
		for (ApplicationDefinition app : vmTemplate.getApplications()) {
			apps.add(app);
		}

		vmTemplate.setApplications(new HashSet<ApplicationDefinition>());
		// Adding empty template and then adding applications (that are already in db)
		// seems to work better for jpa
		vmtRepository.save(vmTemplate);
		for (ApplicationDefinition app : apps) {
			assignApplicationToVmTemplate(vmTemplate.getId(), app.getId());
			// Optional<ApplicationDefinition> manageredApp =
			// appRepository.findById(app.getId());
			// if (manageredApp.isPresent()) {
			// vmTemplate.getApplications().add(manageredApp.get());
			// newApps.add(manageredApp.get());
			// } else {
			// new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
			// "Unable to find application with id=" + app.getId());
			// }
		}
		// vmtRepository.save(vmTemplate);
		vmTemplate.setApplications(apps);
	}

	@Override
	public void addVirtueTemplate(VirtueTemplate template) {
		Collection<VirtualMachineTemplate> vms = new HashSet<VirtualMachineTemplate>();
		for (VirtualMachineTemplate vm : template.getVmTemplates()) {
			vms.add(vm);
		}
		template.setVmTemplates(new HashSet<VirtualMachineTemplate>());
		vtRepository.save(template);
		// adding empty template and then adding vmtempaltes (that are already in db)
		// seem to work better for jpa
		for (VirtualMachineTemplate vmt : vms) {
			assingVmTemplateToVirtueTemplate(template.getId(), vmt.getId());
			// VirtualMachineTemplate managedVmt = vmtRepository.findOne(vmt.getId());
			// template.getVmTemplates().add(managedVmt);
		}
		template.setVmTemplates(vms);
		// vtRepository.save(template);
	}

	@Override
	public void assignVirtueTemplateToUser(VirtueUser user, String virtueTemplateId) {
		// UserName username = new UserName(user.getUsername());
		// TODO this seems inefficient, but it errors if the username does not exist.
		VirtueUser existing = userRepo.findById(user.getUsername()).orElse(null);
		if (existing != null) {
			user = existing;
		}
		VirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		user.addVirtueTemplate(vt);
		userRepo.save(user);
	}

	@Override
	public void revokeVirtueTemplateFromUser(VirtueUser user, String virtueTemplateId) {
		VirtueUser existing = userRepo.findById(user.getUsername()).orElse(null);
		if (existing != null) {
			Iterator<VirtueTemplate> itr = existing.getVirtueTemplates().iterator();
			while (itr.hasNext()) {
				VirtueTemplate template = itr.next();
				if (template.getId().equals(virtueTemplateId)) {
					itr.remove();
					break;
				}
			}
			userRepo.save(existing);
		}
	}

	@Override
	public void assignApplicationToVmTemplate(String vmTemplateId, String applicationId) throws NoSuchElementException {
		VirtualMachineTemplate vmt = vmtRepository.findById(vmTemplateId).get();
		ApplicationDefinition app = appRepository.findById(applicationId).get();
		if (app == null) {
			new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
					"Unable to find application with id=" + applicationId);
		}
		if (vmt == null) {
			new SaviorException(SaviorException.VM_TEMPLATE_NOT_FOUND,
					"Unable to find VM template with id=" + vmTemplateId);
		}
		vmt.getApplications().add(app);
		vmtRepository.save(vmt);
	}

	@Override
	public void assingVmTemplateToVirtueTemplate(String virtueTemplateId, String vmTemplateId)
			throws NoSuchElementException {
		VirtueTemplate vt = vtRepository.findById(virtueTemplateId).get();
		VirtualMachineTemplate vmt = vmtRepository.findById(vmTemplateId).get();
		if (vt == null) {
			new SaviorException(SaviorException.VIRTUE_TEMPLATE_ID_NOT_FOUND,
					"Unable to find application with id=" + virtueTemplateId);
		}
		if (vmt == null) {
			new SaviorException(SaviorException.VM_TEMPLATE_NOT_FOUND,
					"Unable to find VM template with id=" + vmTemplateId);
		}
		vt.getVmTemplates().add(vmt);
		vtRepository.save(vt);
	}

	@Override
	public Collection<String> getUsersWithTemplate() {
		Iterable<VirtueUser> allUsers = userRepo.findAll();
		Iterator<VirtueUser> itr = allUsers.iterator();
		Set<String> users = new HashSet<String>();
		while (itr.hasNext()) {
			VirtueUser user = itr.next();
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
	public Iterable<VirtueTemplate> getVirtueTemplates(Collection<String> vts) {
		return vtRepository.findAllById(vts);
	}

	@Override
	public Iterable<VirtualMachineTemplate> getVmTemplates(Collection<String> vmtIds) {
		return vmtRepository.findAllById(vmtIds);
	}

	@Override
	public Iterable<ApplicationDefinition> getApplications(Collection<String> appIds) {
		return appRepository.findAllById(appIds);
	}

	@Override
	public boolean containsApplication(String id) {
		return appRepository.existsById(id);
	}

	@Override
	public boolean containsVirtueTemplate(String id) {
		return vtRepository.existsById(id);
	}

}
