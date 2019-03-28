/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
import java.util.Set;
import java.util.UUID;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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

	private void initTestDatabase() throws Exception {
		ApplicationDefinition chrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome", "1.0",
				OS.LINUX, null, "google-chrome",null);
		ApplicationDefinition firefox = new ApplicationDefinition(UUID.randomUUID().toString(), "Firefox", "1.0",
				OS.LINUX, null, "firefox",null);
		ApplicationDefinition calculator = new ApplicationDefinition(UUID.randomUUID().toString(), "Calculator", "1.0",
				OS.LINUX, null, "gnome-calculator", null);

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
		// String allTemplate = "default-template";
		String loginUser = "loginUser";
		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Browsers",
				OS.LINUX, "Linux Browsers", appsBrowsers, loginUser, true, now, systemName);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux All", OS.LINUX,
				"Linux All", appsAll, loginUser, true, now, systemName);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Math", OS.LINUX,
				"Linux Math", appsMath, loginUser, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsSingleAll = new HashSet<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);

		// Let's load the cloudformation template file and store it in the virtue.
		String awsCloudformationTemplate = convertStreamToString(
				InMemoryTemplateManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));

		// Add a virtue with the initialized virtual machine.
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Single VM Virtue",
				"1", vmtsSingleAll, awsCloudformationTemplate, "#663399", true, now, systemName);

		Set<VirtualMachineTemplate> vmtsBrowsers = new HashSet<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueSingleBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Browser Virtue",
				"1", vmtsBrowsers, awsCloudformationTemplate, "#808000", true, now, systemName);
		List<VirtualMachineTemplate> vmts = new ArrayList<VirtualMachineTemplate>();
		vmts.add(vmBrowser);
		vmts.add(vmAll);
		vmts.add(vmMath);
		VirtueTemplate virtueAllVms = new VirtueTemplate(UUID.randomUUID().toString(), "Linux All VMs Virtue", "1",
				vmts, awsCloudformationTemplate, "#FF7F50", true, now, systemName);

		Set<VirtualMachineTemplate> vmsMath = new HashSet<VirtualMachineTemplate>();
		vmsMath.add(vmMath);
		VirtueTemplate virtueMath = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Math Virtue", "1",
				vmsMath, awsCloudformationTemplate, "#C0C0C0", true, now, systemName);

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

		VirtueUser user = new VirtueUser("user", new ArrayList<String>(), true);
		VirtueUser user2 = new VirtueUser("user2", new ArrayList<String>(), true);
		VirtueUser user3 = new VirtueUser("user3", new ArrayList<String>(), true);
		VirtueUser admin = new VirtueUser("admin", new ArrayList<String>(), true);
		VirtueUser kdrumm = new VirtueUser("kdrumm", new ArrayList<String>(), true);
		VirtueUser kdrummTest = new VirtueUser("kdrumm_test", new ArrayList<String>(), true);

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
	public ApplicationDefinition getApplicationDefinition(String applicationId) {
		ApplicationDefinition app = applications.get(applicationId);
		if (app == null) {
			throw new SaviorException(SaviorErrorCode.APPLICATION_NOT_FOUND, "not found");
		}
		return app;
	}

	@Override
	public VirtueTemplate getVirtueTemplateForUser(VirtueUser user, String templateId) {
		Collection<String> usersVirtueTemplates = userToTemplateId.get(user.getUsername());
		if (usersVirtueTemplates != null && usersVirtueTemplates.contains(templateId)) {
			return getTemplate(templateId);
		}
		return null;
	}

	@Override
	public VirtueTemplate addVirtueTemplate(VirtueTemplate template) {
		Collection<ApplicationDefinition> apps = template.getApplications();
		verifyAppsExist(apps);
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		verifyVmTemplatesExist(vmts);
		templates.put(template.getId(), template);
		return template;
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
			throw new SaviorException(SaviorErrorCode.VIRTUE_TEMPLATE_NOT_FOUND,
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
			throw new SaviorException(SaviorErrorCode.VIRTUE_TEMPLATE_NOT_FOUND,
					"Unable to find Virtue Template Id=" + virtueTemplateId);
		}
	}

	private VirtueTemplate getTemplate(String templateId) {
		return templates.get(templateId);
	}

	private void verifyVmTemplatesExist(Collection<VirtualMachineTemplate> vmts) {
		for (VirtualMachineTemplate vmt : vmts) {
			if (!vmTemplates.containsKey(vmt.getId())) {
				throw new SaviorException(SaviorErrorCode.VM_TEMPLATE_NOT_FOUND,
						"VM Template ID=" + vmt.getId() + " not found.");
			}
			Collection<ApplicationDefinition> appIds = vmt.getApplications();
			verifyAppsExist(appIds);
		}
	}

	private void verifyAppsExist(Collection<ApplicationDefinition> apps) {
		for (ApplicationDefinition app : apps) {
			if (!applications.containsKey(app.getId())) {
				throw new SaviorException(SaviorErrorCode.APPLICATION_NOT_FOUND,
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
	public VirtueTemplate getVirtueTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public VirtualMachineTemplate getVmTemplate(String templateId) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Iterable<VirtueTemplate> getVirtueTemplates(Collection<String> vts) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Iterable<VirtualMachineTemplate> getVmTemplates(Collection<String> vmtIds) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Iterable<ApplicationDefinition> getApplications(Collection<String> appIds) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean containsApplication(String id) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean containsVirtualMachineTemplate(String id) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean containsVirtueTemplate(String id) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void addIcon(String iconKey, byte[] bytes) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void removeIcon(String iconKey) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public IconModel getIcon(String iconKey) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Set<String> getAllIconKeys() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Iterable<IconModel> getAllIcons() {
		throw new RuntimeException("not implemented");
	}

}
