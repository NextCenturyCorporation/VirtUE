package com.ncc.savior.virtueadmin.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;
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

	private Map<String, JpaVirtueTemplate> templates;
	private Map<String, JpaVirtualMachineTemplate> vmTemplates;
	private Map<String, Collection<String>> userToTemplateId;
	private Map<String, ApplicationDefinition> applications;

	public InMemoryTemplateManager() throws Exception {
		templates = new LinkedHashMap<String, JpaVirtueTemplate>();
		vmTemplates = new LinkedHashMap<String, JpaVirtualMachineTemplate>();
		userToTemplateId = new LinkedHashMap<String, Collection<String>>();
		applications = new LinkedHashMap<String, ApplicationDefinition>();
		initTestDatabase();
	}

	private void initTestDatabase() throws Exception {
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

		Date now = new Date();
		String systemName = "system";
//		String allTemplate = "default-template";
		String loginUser = "loginUser";
		JpaVirtualMachineTemplate vmBrowser = new JpaVirtualMachineTemplate(UUID.randomUUID().toString(),
				"Linux Browsers",
				OS.LINUX, "Linux Browsers", appsBrowsers, loginUser, true, now, systemName);

		JpaVirtualMachineTemplate vmAll = new JpaVirtualMachineTemplate(UUID.randomUUID().toString(), "Linux All",
				OS.LINUX,
				"Linux All", appsAll, loginUser, true, now, systemName);

		JpaVirtualMachineTemplate vmMath = new JpaVirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Math",
				OS.LINUX,
				"Linux Math", appsMath, loginUser, true, now, systemName);

		Set<JpaVirtualMachineTemplate> vmtsSingleAll = new HashSet<JpaVirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);

		// Let's load the cloudformation template file and store it in the virtue.
		String awsCloudformationTemplate = convertStreamToString(
				InMemoryTemplateManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));

		// Add a virtue with the initialized virtual machine.
		JpaVirtueTemplate virtueSingleAll = new JpaVirtueTemplate(UUID.randomUUID().toString(),
				"Linux Single VM Virtue",
				"1.0", vmtsSingleAll, awsCloudformationTemplate, true, now, systemName);

		Set<JpaVirtualMachineTemplate> vmtsBrowsers = new HashSet<JpaVirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		JpaVirtueTemplate virtueSingleBrowsers = new JpaVirtueTemplate(UUID.randomUUID().toString(),
				"Linux Browser Virtue",
				"1.0", vmtsBrowsers, awsCloudformationTemplate, true, now, systemName);
		List<JpaVirtualMachineTemplate> vmts = new ArrayList<JpaVirtualMachineTemplate>();
		vmts.add(vmBrowser);
		vmts.add(vmAll);
		vmts.add(vmMath);
		JpaVirtueTemplate virtueAllVms = new JpaVirtueTemplate(UUID.randomUUID().toString(), "Linux All VMs Virtue",
				"1.0",
				vmts, awsCloudformationTemplate, true, now, systemName);

		Set<JpaVirtualMachineTemplate> vmsMath = new HashSet<JpaVirtualMachineTemplate>();
		vmsMath.add(vmMath);
		JpaVirtueTemplate virtueMath = new JpaVirtueTemplate(UUID.randomUUID().toString(), "Linux Math Virtue", "1.0",
				vmsMath, awsCloudformationTemplate, true, now, systemName);

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

		JpaVirtueUser user = new JpaVirtueUser("user", new ArrayList<String>());
		JpaVirtueUser user2 = new JpaVirtueUser("user2", new ArrayList<String>());
		JpaVirtueUser user3 = new JpaVirtueUser("user3", new ArrayList<String>());
		JpaVirtueUser admin = new JpaVirtueUser("admin", new ArrayList<String>());
		JpaVirtueUser kdrumm = new JpaVirtueUser("kdrumm", new ArrayList<String>());
		JpaVirtueUser kdrummTest = new JpaVirtueUser("kdrumm_test", new ArrayList<String>());

		assignVirtueTemplateToUser(user, virtueAllVms.getId());
		assignVirtueTemplateToUser(user, virtueSingleBrowsers.getId());

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
	}

	@Override
	public Collection<JpaVirtueTemplate> getAllVirtueTemplates() {
		return templates.values();
	}

	@Override
	public Collection<JpaVirtualMachineTemplate> getAllVirtualMachineTemplates() {
		return vmTemplates.values();
	}

	@Override
	public Map<String, JpaVirtueTemplate> getVirtueTemplatesForUser(JpaVirtueUser user) {
		Map<String, JpaVirtueTemplate> map = new LinkedHashMap<String, JpaVirtueTemplate>();
		Collection<String> templateIds = userToTemplateId.get(user.getUsername());
		if (templateIds != null) {
			for (String templateId : templateIds) {
				JpaVirtueTemplate template = templates.get(templateId);
				map.put(template.getId(), template);
			}
		}
		return map;
	}

	@Override
	public Collection<String> getVirtueTemplateIdsForUser(JpaVirtueUser user) {
		return userToTemplateId.get(user.getUsername());
	}

	@Override
	public Optional<ApplicationDefinition> getApplicationDefinition(String applicationId) {
		return Optional.of(applications.get(applicationId));
	}

	@Override
	public JpaVirtueTemplate getVirtueTemplateForUser(JpaVirtueUser user, String templateId) {
		Collection<String> userTemplates = userToTemplateId.get(user.getUsername());
		if (userTemplates != null && userTemplates.contains(templateId)) {
			return getTemplate(templateId);
		}
		return null;
	}

	@Override
	public void addVirtueTemplate(JpaVirtueTemplate template) {
		Collection<ApplicationDefinition> apps = template.getApplications();
		verifyAppsExist(apps);
		Collection<JpaVirtualMachineTemplate> vmts = template.getVmTemplates();
		verifyVmTemplatesExist(vmts);
		templates.put(template.getId(), template);
	}

	@Override
	public void addVmTemplate(JpaVirtualMachineTemplate vmTemplate) {
		Collection<ApplicationDefinition> apps = vmTemplate.getApplications();
		verifyAppsExist(apps);
		vmTemplates.put(vmTemplate.getId(), vmTemplate);
	}

	@Override
	public void addApplicationDefinition(ApplicationDefinition app) {
		applications.put(app.getId(), app);
	}

	@Override
	public void assignVirtueTemplateToUser(JpaVirtueUser user, String virtueTemplateId) {
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
	public void revokeVirtueTemplateFromUser(JpaVirtueUser user, String virtueTemplateId) {
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

	private JpaVirtueTemplate getTemplate(String templateId) {
		return templates.get(templateId);
	}

	private void verifyVmTemplatesExist(Collection<JpaVirtualMachineTemplate> vmts) {
		for (JpaVirtualMachineTemplate vmt : vmts) {
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
	// Convert a stream into a single, newline separated string
	public static String convertStreamToString(InputStream in) throws Exception {

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder stringbuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringbuilder.append(line + "\n");
		}
		in.close();
		return stringbuilder.toString();
	}

	@Override
	public Iterable<ApplicationDefinition> getAllApplications() {
		return applications.values();
	}

	@Override
	public void assignApplicationToVmTemplate(String vmTemplateId, String applicationId) {
		JpaVirtualMachineTemplate vm = this.vmTemplates.get(vmTemplateId);
		ApplicationDefinition app = this.applications.get(applicationId);
		if (vm != null && app != null) {
			vm.getApplications().add(app);
		}
	}

	@Override
	public void assingVmTemplateToVirtueTemplate(String virtueTemplateId, String vmTemplateId) {
		JpaVirtualMachineTemplate vm = this.vmTemplates.get(vmTemplateId);
		JpaVirtueTemplate virtue = this.templates.get(virtueTemplateId);
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
	public Optional<JpaVirtueTemplate> getVirtueTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Optional<JpaVirtualMachineTemplate> getVmTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void test() {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<JpaVirtualMachineTemplate> getVmTemplatesById(Collection<String> vmTemplateIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ApplicationDefinition> getApplicationDefinitions(Collection<String> applicationIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
