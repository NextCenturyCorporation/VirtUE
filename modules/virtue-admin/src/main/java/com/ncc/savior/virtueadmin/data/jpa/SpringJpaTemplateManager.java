package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * {@link ITemplateManager} that uses Spring and JPA.
 *
 * Note that this is  SpringJpaUserManager.
 */
@Repository
public class SpringJpaTemplateManager implements ITemplateManager {
	private final static Logger logger = LoggerFactory.getLogger(SpringJpaTemplateManager.class);

	@Autowired
	private VirtueTemplateRepository virtueTemplateRepo;
	@Autowired
	private ApplicationDefinitionRepository appRepo;
	@Autowired
	private VirtualMachineTemplateRepository vmTemplateRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private IconRepository iconRepo;

	public SpringJpaTemplateManager(VirtueTemplateRepository virtueTemplateRepo,
			VirtualMachineTemplateRepository vmTemplateRepo, ApplicationDefinitionRepository appRepo,
			UserRepository userRepo) {
		this.virtueTemplateRepo = virtueTemplateRepo;
		this.vmTemplateRepo = vmTemplateRepo;
		this.appRepo = appRepo;
		this.userRepo = userRepo;
	}

	/**
	 * This doesn't care whether the user exists on the backend.
	 */
	@Override
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(VirtueUser user) {
		Collection<VirtueTemplate> templates = user.getVirtueTemplates();
		Map<String, VirtueTemplate> map = new HashMap<String, VirtueTemplate>();
		for (VirtueTemplate t : templates) {
			map.put(t.getId(), t);
		}
		return map;
	}

	/**
	 * This doesn't care whether the user exists on the backend.
	 */
	@Override
	public Collection<String> getVirtueTemplateIdsForUser(VirtueUser user) {
		// return user.getVirtueTemplateIds();
		/**
		 * See commented line - the code below does the same thing as getVirtueTemplateIds, except the latter doesn't use a hashset.
		 * Do we really need that?
		 */
		Collection<VirtueTemplate> templates = user.getVirtueTemplates();
		Collection<String> map = new HashSet<String>();
		for (VirtueTemplate t : templates) {
			map.add(t.getId());
		}
		return map;
	}

	/**
	 */
	@Override
	public VirtueTemplate getVirtueTemplateForUser(VirtueUser user, String templateId) {
		for (VirtueTemplate template : user.getVirtueTemplates()) {
			if (template.getId().equals(templateId)) {
				return template;
			}
		}
		throw new SaviorException(SaviorErrorCode.VIRTUE_TEMPLATE_NOT_FOUND,
				"Virtue Template id=" + templateId + " not found.");
	}

 	@Override
	public Iterable<VirtueTemplate> getAllVirtueTemplates() {
		return virtueTemplateRepo.findAll();
	}

 	@Override
	public Iterable<VirtualMachineTemplate> getAllVirtualMachineTemplates() {
		return vmTemplateRepo.findAll();
	}

 	@Override
	public Iterable<ApplicationDefinition> getAllApplications() {
		return appRepo.findAll();
	}

 	/**
	 * It doesn't look like there's a reason to use Optionals here. */
	@Override
	public ApplicationDefinition getApplicationDefinition(String applicationId) {
		Optional<ApplicationDefinition> oa = appRepo.findById(applicationId);
		if (oa.isPresent()) {
			return oa.get();
		} else {
			throw new SaviorException(SaviorErrorCode.APPLICATION_NOT_FOUND,
					"Cannot find application with id=" + applicationId);
		}
	}

 	/**
	 * It doesn't look like there's a reason to use Optionals here. */
	@Override
	public VirtualMachineTemplate getVmTemplate(String templateId) {
		Optional<VirtualMachineTemplate> ovmt = vmTemplateRepo.findById(templateId);
		if (ovmt.isPresent()) {
			return ovmt.get();
		} else {
			throw new SaviorException(SaviorErrorCode.VM_TEMPLATE_NOT_FOUND,
					"Cannot find VM template with id=" + templateId);
		}
	}

 	/**
	 * It doesn't look like there's a reason to use Optionals here. */
	@Override
	public VirtueTemplate getVirtueTemplate(String templateId) {
		Optional<VirtueTemplate> ovt = virtueTemplateRepo.findById(templateId);
		if (ovt.isPresent()) {
			return ovt.get();
		} else {
			throw new SaviorException(SaviorErrorCode.VIRTUE_TEMPLATE_NOT_FOUND,
					"Cannot find virtue template with id=" + templateId);
		}
	}

 	/**
	 * It doesn't look like there's a reason to use Optionals here.
	 * Made not private and not included in interface, because it has same functionality as SpringJpaUserManager.getUser
	 */
	private VirtueUser getUser(String username) {
		Optional<VirtueUser> ovt = userRepo.findById(username);
		if (ovt.isPresent()) {
			return ovt.get();
		} else {
			throw new SaviorException(SaviorErrorCode.USER_NOT_FOUND,
					"Cannot find user with username=" + username);
		}
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		appRepo.save(app);
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
		vmTemplateRepo.save(vmTemplate);
		for (ApplicationDefinition app : apps) {
			assignApplicationToVmTemplate(vmTemplate.getId(), app.getId());
			// Optional<ApplicationDefinition> manageredApp =
			// appRepo.findById(app.getId());
			// if (manageredApp.isPresent()) {
			// vmTemplate.getApplications().add(manageredApp.get());
			// newApps.add(manageredApp.get());
			// } else {
			// new SaviorException(SaviorException.APPLICATION_NOT_FOUND,
			// "Unable to find application with id=" + app.getId());
			// }
		}
		// vmTemplateRepo.save(vmTemplate); // why is this commented out too? (see addVirtueTemplate function)
		//
		vmTemplate.setApplications(apps);
	}

	@Override
	public VirtueTemplate addVirtueTemplate(VirtueTemplate template) {
		Collection<VirtualMachineTemplate> vms = new HashSet<VirtualMachineTemplate>();
		for (VirtualMachineTemplate vm : template.getVmTemplates()) {
			vms.add(vm);
		}
		template.setVmTemplates(new HashSet<VirtualMachineTemplate>());

		VirtueTemplate savedTemplate = virtueTemplateRepo.save(template);

		// adding empty template and then adding vmtempaltes (that are already in db)
		// seem to work better for jpa
		for (VirtualMachineTemplate vmt : vms) {
			assingVmTemplateToVirtueTemplate(template.getId(), vmt.getId());
			// VirtualMachineTemplate managedVmt = vmTemplateRepo.findOne(vmt.getId());
			// template.getVmTemplates().add(managedVmt);
		}
		template.setVmTemplates(vms);
		// virtueTemplateRepo.save(template); // TODO how do changes made after saving the object propogate to that object?
		return savedTemplate;
	}

	@Override
	public void assignVirtueTemplateToUser(VirtueUser user, String virtueTemplateId) {
		// This would let you add something to a user that hasn't actually been saved to the backend yet.
		// But it's different from how everything else works, and we don't actually use it.
		// VirtueUser existing = userRepo.findById(user.getUsername()).orElse(null);
		// if (existing != null) {
		// 	user = existing;
		// }


		// make sure that the only change made is the addition of the virtueTemplate.
		user = getUser(user.getUsername());
		VirtueTemplate vt = getVirtueTemplate(virtueTemplateId);
		user.addVirtueTemplate(vt);
		userRepo.save(user);
	}


	@Override
	public void revokeVirtueTemplateFromUser(VirtueUser user, String virtueTemplateId) {
		assertUserExists(user.getUsername());
		Iterator<VirtueTemplate> itr = user.getVirtueTemplates().iterator();
		while (itr.hasNext()) {
			VirtueTemplate template = itr.next();
			if (template.getId().equals(virtueTemplateId)) {
				itr.remove();
				break;
			}
		}
		userRepo.save(user);
	}

	@Override
	public void assignApplicationToVmTemplate(String vmTemplateId, String applicationId) throws NoSuchElementException {
		VirtualMachineTemplate vmTemplate = getVmTemplate(vmTemplateId);
		ApplicationDefinition app = getApplicationDefinition(applicationId);
		vmTemplate.getApplications().add(app); // #TODO add a method for adding an app - check ID uniqueness there.
		vmTemplateRepo.save(vmTemplate);
	}

	@Override
	public void assingVmTemplateToVirtueTemplate(String virtueTemplateId, String vmTemplateId)
			throws NoSuchElementException {
		VirtueTemplate virtueTemplate = getVirtueTemplate(virtueTemplateId);
		VirtualMachineTemplate vmTemplate = getVmTemplate(vmTemplateId);
		virtueTemplate.getVmTemplates().add(vmTemplate);// #TODO add a method for adding an vmTemplate - check ID uniqueness there.
		virtueTemplateRepo.save(virtueTemplate);
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
		virtueTemplateRepo.deleteAll();
		vmTemplateRepo.deleteAll();
		appRepo.deleteAll();
	}

	@Override
	public void deleteApplicationDefinition(String templateId) {
		appRepo.deleteById(templateId);
	}

	@Override
	public void deleteVmTemplate(String templateId) {
		vmTemplateRepo.deleteById(templateId);
	}

	@Override
	public void deleteVirtueTemplate(String templateId) {
		virtueTemplateRepo.deleteById(templateId);
	}

	@Override
	public Iterable<VirtueTemplate> getVirtueTemplates(Collection<String> vts) {
		return virtueTemplateRepo.findAllById(vts);
	}

	@Override
	public Iterable<VirtualMachineTemplate> getVmTemplates(Collection<String> vmtIds) {
		return vmTemplateRepo.findAllById(vmtIds);
	}

	@Override
	public Iterable<ApplicationDefinition> getApplications(Collection<String> appIds) {
		return appRepo.findAllById(appIds);
	}

	@Override
	public boolean containsApplication(String id) {
		return appRepo.existsById(id);
	}

	@Override
	public boolean containsVirtualMachineTemplate(String id) {
		return vmTemplateRepo.existsById(id);
	}

	@Override
	public boolean containsVirtueTemplate(String id) {
		return virtueTemplateRepo.existsById(id);
	}

	@Override
	public void addIcon(String iconKey, byte[] bytes) {
		IconModel icon = new IconModel(iconKey, bytes);
		iconRepo.save(icon);
	}

	@Override
	public Iterable<IconModel> getAllIcons() {
		return iconRepo.findAll();
	}

	@Override
	public IconModel getIcon(String iconKey) {
		Optional<IconModel> icon = iconRepo.findById(iconKey);
		if (icon.isPresent()) {
			return icon.get();
		} else {
			return null;
		}
	}

	@Override
	public void removeIcon(String iconKey) {
		iconRepo.deleteById(iconKey);
	}

	@Override
	public Set<String> getAllIconKeys() {
		Iterable<IconModel> itra = iconRepo.findAll();
		Set<String> set = new HashSet<String>();
		itra.forEach((IconModel im) -> {
			set.add(im.getId());
		});
		return set;
	}



	private void assertUserExists(String username) {
		if (userRepo.findById(username) == null) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_FOUND,
					"Cannot find user with username=" + username);
		}
	}

}
