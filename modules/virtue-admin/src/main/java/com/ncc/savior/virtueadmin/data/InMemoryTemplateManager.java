package com.ncc.savior.virtueadmin.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * Implementation of {@link ITemplateManager} that stores all the template data
 * in memory. This implementation is for testing and demo purpose only and
 * should not be used in production.
 * 
 * See interface for function comments.
 *
 */
public class InMemoryTemplateManager implements ITemplateManager {

	private Map<String, VirtueTemplate> templates;
	private Map<String, VirtualMachineTemplate> vmTemplates;
	private Map<String, Collection<String>> userToTemplateId;
	private Map<String, ApplicationDefinition> applications;

	public InMemoryTemplateManager() {
		templates = new LinkedHashMap<String, VirtueTemplate>();
		vmTemplates = new LinkedHashMap<String, VirtualMachineTemplate>();
		userToTemplateId = new LinkedHashMap<String, Collection<String>>();
		applications = new LinkedHashMap<String, ApplicationDefinition>();
		initTestDatabase();
	}

	private void initTestDatabase() {
		ApplicationDefinition chrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome", "1.0",
				OS.LINUX, "google-chrome");
		ApplicationDefinition firefox = new ApplicationDefinition(UUID.randomUUID().toString(), "Firefox", "1.0",
				OS.LINUX, "firefox");
		ApplicationDefinition calculator = new ApplicationDefinition(UUID.randomUUID().toString(), "Calculator", "1.0",
				OS.LINUX, "gnome-calculator");

		Collection<ApplicationDefinition> appsAll = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsBrowsers = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsMath = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appChromeIsBetterThanFirefox = new LinkedList<ApplicationDefinition>();

		appsAll.add(chrome);
		appsAll.add(firefox);
		appsAll.add(calculator);
		appsBrowsers.add(chrome);
		appsBrowsers.add(firefox);
		appChromeIsBetterThanFirefox.add(chrome);
		appChromeIsBetterThanFirefox.add(calculator);
		appsMath.add(calculator);

		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Browsers",
				OS.LINUX, "Linux Browsers", appsBrowsers);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux All", OS.LINUX,
				"Linux All", appsAll);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Math", OS.LINUX,
				"Linux Math", appsMath);

		List<VirtualMachineTemplate> vmtsSingleAll = new ArrayList<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Single VM Virtue",
				"1.0", vmtsSingleAll);
		List<VirtualMachineTemplate> vmtsBrowsers = new ArrayList<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueSingleBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Browser Virtue",
				"1.0", vmtsBrowsers);
		List<VirtualMachineTemplate> vmts = new ArrayList<VirtualMachineTemplate>();
		vmts.add(vmBrowser);
		vmts.add(vmAll);
		vmts.add(vmMath);
		VirtueTemplate virtueAllVms = new VirtueTemplate(UUID.randomUUID().toString(), "Linux All VMs Virtue", "1.0",
				vmts);

		addApplicationDefinition(calculator);
		addApplicationDefinition(firefox);
		addApplicationDefinition(chrome);

		addVmTemplate(vmAll);
		addVmTemplate(vmMath);
		addVmTemplate(vmBrowser);

		addVirtueTemplate(virtueAllVms);
		addVirtueTemplate(virtueSingleBrowsers);
		addVirtueTemplate(virtueSingleAll);

		assignVirtueTemplateToUser(User.testUser(), virtueAllVms.getId());
		assignVirtueTemplateToUser(User.testUser(), virtueSingleBrowsers.getId());

	}

	@Override
	public Collection<VirtueTemplate> getAllVirtueTemplates() {
		return templates.values();
	}

	@Override
	public Collection<VirtualMachineTemplate> getAllVirtualMachineTemplates() {
		return vmTemplates.values();
	}

	@Override
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(User user) {
		Map<String, VirtueTemplate> map = new LinkedHashMap<String, VirtueTemplate>();
		Collection<String> templateIds = userToTemplateId.get(user.getUsername());
		for (String templateId : templateIds) {
			VirtueTemplate template = templates.get(templateId);
			map.put(template.getId(), template);
		}
		return map;
	}

	@Override
	public Collection<String> getVirtueTemplateIdsForUser(User user) {
		return userToTemplateId.get(user.getUsername());
	}

	@Override
	public ApplicationDefinition getApplicationDefinition(String applicationId) {
		return applications.get(applicationId);
	}

	@Override
	public VirtueTemplate getTemplate(User user, String templateId) {
		if (userToTemplateId.get(user.getUsername()).contains(templateId)) {
			return getTemplate(templateId);
		}
		return null;
	}

	@Override
	public void addVirtueTemplate(VirtueTemplate template) {
		Collection<ApplicationDefinition> apps = template.getApplications();
		verifyAppsExist(apps);
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		verifyVmTemplatesExist(vmts);
		templates.put(template.getId(), template);
	}

	@Override
	public void addVmTemplate(VirtualMachineTemplate vmTemplate) {
		Collection<ApplicationDefinition> apps = vmTemplate.getApplications();
		verifyAppsExist(apps);
		vmTemplates.put(vmTemplate.getId(), vmTemplate);
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		applications.put(app.getId(), app);
	}

	@Override
	public void assignVirtueTemplateToUser(User user, String virtueTemplateId) {
		if (templates.containsKey(virtueTemplateId)) {
			Collection<String> list = userToTemplateId.get(user.getUsername());
			if (list == null) {
				list = new LinkedHashSet<String>();
				userToTemplateId.put(user.getUsername(), list);
			}
			list.add(virtueTemplateId);
		} else {
			throw new SaviorException(SaviorException.VIRTUE_TEMPLATE_ID_NOT_FOUND,
					"Unable to find Virtue Template Id=" + virtueTemplateId);
		}
	}

	@Override
	public void revokeVirtueTemplateFromUser(User user, String virtueTemplateId) {
		if (templates.containsKey(virtueTemplateId)) {
			Collection<String> list = userToTemplateId.get(user.getUsername());
			if (list != null) {
				list.remove(virtueTemplateId);
			}
		} else {
			throw new SaviorException(SaviorException.VIRTUE_TEMPLATE_ID_NOT_FOUND,
					"Unable to find Virtue Template Id=" + virtueTemplateId);
		}
	}

	private VirtueTemplate getTemplate(String templateId) {
		return templates.get(templateId);
	}

	private void verifyVmTemplatesExist(Collection<VirtualMachineTemplate> vmts) {
		for (VirtualMachineTemplate vmt : vmts) {
			if (!vmTemplates.containsKey(vmt.getId())) {
				throw new SaviorException(SaviorException.VM_TEMPLATE_NOT_FOUND,
						"VM Template ID=" + vmt.getId() + " not found.");
			}
			Collection<ApplicationDefinition> appIds = vmt.getApplications();
			verifyAppsExist(appIds);
		}
	}

	private void verifyAppsExist(Collection<ApplicationDefinition> apps) {
		for (ApplicationDefinition app : apps) {
			if (!applications.containsKey(app.getId())) {
				throw new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
						"Application ID=" + app + " not found.");
			}
		}
	}

	@Override
	public Iterable<ApplicationDefinition> getAllApplications() {
		return applications.values();
	}

	@Override
	public void assignApplicationToVmTemplate(String vmTemplateId, String applicationId) {
		VirtualMachineTemplate vm = this.vmTemplates.get(vmTemplateId);
		ApplicationDefinition app = this.applications.get(applicationId);
		if (vm != null && app != null) {
			vm.getApplications().add(app);
		}
	}

	@Override
	public void assingVmTemplateToVirtueTemplate(String virtueTemplateId, String vmTemplateId) {
		VirtualMachineTemplate vm = this.vmTemplates.get(vmTemplateId);
		VirtueTemplate virtue = this.templates.get(virtueTemplateId);
		if (virtue != null && vm != null) {
			virtue.getVmTemplates().add(vm);
		}
	}

}
