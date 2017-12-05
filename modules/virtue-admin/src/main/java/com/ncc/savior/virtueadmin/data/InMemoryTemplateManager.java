package com.ncc.savior.virtueadmin.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		Map<String, ApplicationDefinition> appsAll = new LinkedHashMap<String, ApplicationDefinition>();
		Map<String, ApplicationDefinition> appsBrowsers = new LinkedHashMap<String, ApplicationDefinition>();
		Map<String, ApplicationDefinition> appsMath = new LinkedHashMap<String, ApplicationDefinition>();
		Map<String, ApplicationDefinition> appChromeIsBetterThanFirefox = new LinkedHashMap<String, ApplicationDefinition>();

		appsAll.put(chrome.getId(), chrome);
		appsAll.put(firefox.getId(), firefox);
		appsAll.put(calculator.getId(), calculator);
		appsBrowsers.put(chrome.getId(), chrome);
		appsBrowsers.put(firefox.getId(), firefox);
		appChromeIsBetterThanFirefox.put(chrome.getId(), chrome);
		appChromeIsBetterThanFirefox.put(calculator.getId(), calculator);
		appsMath.put(calculator.getId(), calculator);

		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Browsers",
				OS.LINUX, "Linux Browsers", appsBrowsers);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux All", OS.LINUX,
				"Linux All", appsAll);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Math", OS.LINUX,
				"Linux Math", appsMath);

		List<VirtualMachineTemplate> vmtsSingleAll = new ArrayList<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Single VM Virtue",
				"1.0", appsAll, vmtsSingleAll);
		List<VirtualMachineTemplate> vmtsBrowsers = new ArrayList<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueSingleBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Browser Virtue",
				"1.0", appsBrowsers, vmtsBrowsers);
		List<VirtualMachineTemplate> vmts = new ArrayList<VirtualMachineTemplate>();
		vmts.add(vmBrowser);
		vmts.add(vmAll);
		vmts.add(vmMath);
		VirtueTemplate virtueAllVms = new VirtueTemplate(UUID.randomUUID().toString(), "Linux All VMs Virtue", "1.0",
				appsAll, vmts);

		List<VirtualMachineTemplate> vmsMath = new ArrayList<VirtualMachineTemplate>();
		vmsMath.add(vmMath);
		VirtueTemplate virtueMath = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Math Virtue", "1.0",
				appsMath, vmsMath);

		addApplicationDefinition(calculator);
		addApplicationDefinition(firefox);
		addApplicationDefinition(chrome);

		addVmTemplate(vmAll);
		addVmTemplate(vmMath);
		addVmTemplate(vmBrowser);

		addVirtueTemplate(virtueAllVms);
		addVirtueTemplate(virtueSingleBrowsers);
		addVirtueTemplate(virtueSingleAll);
		addVirtueTemplate(virtueMath);

		User user = new User("user", new ArrayList<String>());
		User user2 = new User("user2", new ArrayList<String>());
		User user3 = new User("user3", new ArrayList<String>());
		User admin = new User("admin", new ArrayList<String>());
		User kdrumm = new User("kdrumm", new ArrayList<String>());
		User kdrummTest = new User("kdrumm_test", new ArrayList<String>());

		assignVirtueTemplateToUser(user, virtueAllVms.getId());
		assignVirtueTemplateToUser(user, virtueSingleBrowsers.getId());

		assignVirtueTemplateToUser(kdrumm, virtueAllVms.getId());
		assignVirtueTemplateToUser(kdrumm, virtueSingleBrowsers.getId());

		assignVirtueTemplateToUser(kdrummTest, virtueMath.getId());

		assignVirtueTemplateToUser(user2, virtueSingleBrowsers.getId());

		assignVirtueTemplateToUser(user3, virtueMath.getId());

		assignVirtueTemplateToUser(admin, virtueAllVms.getId());
		assignVirtueTemplateToUser(admin, virtueSingleBrowsers.getId());
		assignVirtueTemplateToUser(admin, virtueSingleAll.getId());
		assignVirtueTemplateToUser(admin, virtueMath.getId());

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
		if (templateIds != null) {
			for (String templateId : templateIds) {
				VirtueTemplate template = templates.get(templateId);
				map.put(template.getId(), template);
			}
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
		Set<String> appIds = template.getApplications().keySet();
		verifyAppsExist(appIds);
		List<VirtualMachineTemplate> vmts = template.getVmTemplates();
		verifyVmTemplatesExist(vmts);
		templates.put(template.getId(), template);
	}

	@Override
	public void addVmTemplate(VirtualMachineTemplate vmTemplate) {
		Set<String> appIds = vmTemplate.getApplications().keySet();
		verifyAppsExist(appIds);
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

	private void verifyVmTemplatesExist(List<VirtualMachineTemplate> vmts) {
		for (VirtualMachineTemplate vmt : vmts) {
			if (!vmTemplates.containsKey(vmt.getId())) {
				throw new SaviorException(SaviorException.VM_TEMPLATE_NOT_FOUND,
						"VM Template ID=" + vmt.getId() + " not found.");
			}
			Set<String> appIds = vmt.getApplications().keySet();
			verifyAppsExist(appIds);
		}
	}

	private void verifyAppsExist(Set<String> appIds) {
		for (String appId : appIds) {
			if (!applications.containsKey(appId)) {
				throw new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
						"Application ID=" + appId + " not found.");
			}
		}
	}

}
