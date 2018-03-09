package com.ncc.savior.virtueadmin.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
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

	public InMemoryTemplateManager() throws Exception {
		templates = new LinkedHashMap<String, VirtueTemplate>();
		vmTemplates = new LinkedHashMap<String, VirtualMachineTemplate>();
		userToTemplateId = new LinkedHashMap<String, Collection<String>>();
		applications = new LinkedHashMap<String, ApplicationDefinition>();
		initTestDatabase();
	}

	private void initTestDatabase() throws Exception  {
		ApplicationDefinition chrome = new ApplicationDefinition("Chrome", "1.0",
				OS.LINUX, "google-chrome");
		ApplicationDefinition firefox = new ApplicationDefinition("Firefox", "1.0",
				OS.LINUX, "firefox");
		ApplicationDefinition calculator = new ApplicationDefinition("Calculator", "1.0",
				OS.LINUX, "gnome-calculator");
		ApplicationDefinition edge = new ApplicationDefinition("Edge", "1.0", OS.WINDOWS, "edge.exe");

		Set<ApplicationDefinition> appsAll = new HashSet<ApplicationDefinition>();
		Set<ApplicationDefinition> appsBrowsers = new HashSet<ApplicationDefinition>();
		Set<ApplicationDefinition> appsMath = new HashSet<ApplicationDefinition>();

		appsBrowsers.add(chrome);
		appsBrowsers.add(firefox);
		appsMath.add(calculator);

		appsAll.addAll(appsBrowsers);
		appsAll.addAll(appsMath);

		Date now = new Date();
		String systemName = "system";
//		String allTemplate = "default-template";
		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Browsers",
				OS.LINUX, "Linux Browsers", appsBrowsers, true, now, systemName);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux All", OS.LINUX,
				"Linux All", appsAll, true, now, systemName);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Math", OS.LINUX,
				"Linux Math", appsMath, true, now, systemName);
		
		VirtualMachineTemplate vmWinBrowser = new VirtualMachineTemplate("Windows Browsers", OS.WINDOWS, "Windows Browsers", Collections.singleton(edge), true, now, systemName);

		Set<VirtualMachineTemplate> vmtsSingleAll = new HashSet<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);

		// Let's load the cloudformation template files and store them in the virtue.
		String awsLinuxCloudformationTemplate = convertStreamToString(
				InMemoryTemplateManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));

		// Add a virtue with the initialized virtual machine.
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Single VM Virtue",
				"1.0", vmtsSingleAll, awsLinuxCloudformationTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsBrowsers = new HashSet<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueSingleBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Browser Virtue",
				"1.0", vmtsBrowsers, awsLinuxCloudformationTemplate, true, now, systemName);
		List<VirtualMachineTemplate> vmts = new ArrayList<VirtualMachineTemplate>();
		vmts.add(vmBrowser);
		vmts.add(vmAll);
		vmts.add(vmMath);
		VirtueTemplate virtueAllVms = new VirtueTemplate(UUID.randomUUID().toString(), "Linux All VMs Virtue", "1.0",
				vmts, awsLinuxCloudformationTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmsMath = new HashSet<VirtualMachineTemplate>();
		vmsMath.add(vmMath);
		VirtueTemplate virtueMath = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Math Virtue", "1.0",
				vmsMath, awsLinuxCloudformationTemplate, true, now, systemName);

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

		// Windows VM & Virtue
		String awsWindowsCloudformationTemplate = convertStreamToString(
				InMemoryTemplateManager.class.getResourceAsStream("/aws-templates/WindowsVM.template"));
		VirtueTemplate virtueWinBrowser = new VirtueTemplate("Windows Browser", "1.0",
				Collections.singleton(vmWinBrowser), awsWindowsCloudformationTemplate, true, now, systemName);
		addVirtueTemplate(virtueWinBrowser);

		VirtueUser user = new VirtueUser("user", new ArrayList<String>());
		VirtueUser user2 = new VirtueUser("user2", new ArrayList<String>());
		VirtueUser user3 = new VirtueUser("user3", new ArrayList<String>());
		VirtueUser admin = new VirtueUser("admin", new ArrayList<String>());
		VirtueUser kdrumm = new VirtueUser("kdrumm", new ArrayList<String>());
		VirtueUser kdrummTest = new VirtueUser("kdrumm_test", new ArrayList<String>());

		assignVirtueTemplateToUser(user, virtueAllVms.getId());
		assignVirtueTemplateToUser(user, virtueSingleBrowsers.getId());
		assignVirtueTemplateToUser(user, awsWindowsCloudformationTemplate);

		assignVirtueTemplateToUser(kdrumm, virtueAllVms.getId());
		assignVirtueTemplateToUser(kdrumm, virtueSingleBrowsers.getId());

		assignVirtueTemplateToUser(kdrummTest, virtueMath.getId());

		assignVirtueTemplateToUser(user2, virtueSingleBrowsers.getId());

		assignVirtueTemplateToUser(user3, virtueMath.getId());

		System.out.println("VirtueAllVms  - " + virtueAllVms.getId());
		assignVirtueTemplateToUser(admin, virtueAllVms.getId());

		System.out.println("virtueSingleBrowsers  - " + virtueSingleBrowsers.getId());
		assignVirtueTemplateToUser(admin, virtueSingleBrowsers.getId());

		System.out.println("virtueSingleAll  - " + virtueSingleAll.getId());
		assignVirtueTemplateToUser(admin, virtueSingleAll.getId());

		System.out.println("virtueMath  - " + virtueMath.getId());
		assignVirtueTemplateToUser(admin, virtueMath.getId());
		
		assignVirtueTemplateToUser(admin, virtueWinBrowser.getId());
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
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(VirtueUser user) {
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
	public Collection<String> getVirtueTemplateIdsForUser(VirtueUser user) {
		return userToTemplateId.get(user.getUsername());
	}

	@Override
	public Optional<ApplicationDefinition> getApplicationDefinition(String applicationId) {
		return Optional.of(applications.get(applicationId));
	}

	@Override
	public VirtueTemplate getVirtueTemplateForUser(VirtueUser user, String templateId) {
		Collection<String> userTemplates = userToTemplateId.get(user.getUsername());
		if (userTemplates != null && userTemplates.contains(templateId)) {
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
	public void assignVirtueTemplateToUser(VirtueUser user, String virtueTemplateId) {
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
	public void revokeVirtueTemplateFromUser(VirtueUser user, String virtueTemplateId) {
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

	/* Aws Specific methods */
	// Convert a stream into a single string
	public static String convertStreamToString(InputStream in) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			return reader.lines().collect(Collectors.joining("\n"));
		} finally {
			if (reader != null) {
				reader.close();
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

	@Override
	public Collection<String> getUsersWithTemplate() {
		return userToTemplateId.keySet();
	}

	@Override
	public void clear() {
		templates.clear();
		vmTemplates.clear();
		userToTemplateId.clear();
		applications.clear();
	}

	@Override
	public void deleteApplicationDefinition(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void deleteVmTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void deleteVirtueTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Optional<VirtueTemplate> getVirtueTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Optional<VirtualMachineTemplate> getVmTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

}
