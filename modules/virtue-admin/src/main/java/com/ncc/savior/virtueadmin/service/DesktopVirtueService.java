package com.ncc.savior.virtueadmin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.infrastructure.IApplicationManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;
import com.ncc.savior.virtueadmin.security.SecurityUserService;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

/**
 * Virtue service to handle functions specifically for the desktop application.
 * 
 *
 */
public class DesktopVirtueService {
	private static final Logger logger = LoggerFactory.getLogger(DesktopVirtueService.class);
	private IActiveVirtueManager activeVirtueManager;
	private ITemplateManager templateManager;
	private IApplicationManager applicationManager;

	@Autowired
	private SecurityUserService securityService;

	@Value("${virtue.aws.windows.password}")
	private String windowsPassword;

	public DesktopVirtueService(IActiveVirtueManager activeVirtueManager, ITemplateManager templateManager,
			IApplicationManager applicationManager) {
		this.activeVirtueManager = activeVirtueManager;
		this.templateManager = templateManager;
		this.applicationManager = applicationManager;
	}

	/**
	 * Gets all virtues as {@link DesktopVirtue}s for the user including Virtues
	 * that have not been provisioned yet, but the user has the ability to
	 * provision.
	 * 
	 * @return
	 * 
	 */
	public Set<DesktopVirtue> getDesktopVirtuesForUser() {
		VirtueUser user = verifyAndReturnUser();
		Map<String, VirtueTemplate> templates = templateManager.getVirtueTemplatesForUser(user);
		Map<String, Set<VirtueInstance>> templateIdToActiveVirtues = activeVirtueManager.getVirtuesFromTemplateIds(user,
				templates.keySet());
		Set<DesktopVirtue> virtues = new LinkedHashSet<DesktopVirtue>();
		for (String templateId : templates.keySet()) {
			if (templateIdToActiveVirtues.containsKey(templateId)) {
				// Template has active virtues
				for (VirtueInstance instance : templateIdToActiveVirtues.get(templateId)) {
					// convert VirtueInstance to DesktopVirtue and add
					DesktopVirtue dv = convertVirtueInstanceToDesktopVirtue(instance);
					virtues.add(dv);
				}
			} else {
				VirtueTemplate template = templates.get(templateId);
				DesktopVirtue dv = convertVirtueTemplateToDesktopVirtue(template);
				virtues.add(dv);
			}
		}
		return virtues;
	}

	public Set<DesktopVirtue> getVirtueApplicationsByUserAndTag(String tag) {
		Set<DesktopVirtue> all = getDesktopVirtuesForUser();
		HashSet<DesktopVirtue> resultantVirtues = new HashSet<DesktopVirtue>();
		Iterator<DesktopVirtue> vItr = all.iterator();
		while (vItr.hasNext()) {
			DesktopVirtue virtue = vItr.next();
			Iterator<Entry<String, ApplicationDefinition>> appItr = virtue.getApps().entrySet().iterator();
			HashMap<String, ApplicationDefinition> apps = new HashMap<String, ApplicationDefinition>();
			while (appItr.hasNext()) {
				ApplicationDefinition app = appItr.next().getValue();
				if (app.getTags().contains(tag)) {
					apps.put(app.getId(), app);
				}
			}
			if (!apps.isEmpty()) {
				virtue.setApps(apps);
				resultantVirtues.add(virtue);
			}
		}
		return resultantVirtues;
	}

	public DesktopVirtueApplication startApplication(String virtueId, String applicationId, String params) {
		verifyAndReturnUser();
		ApplicationDefinition application = templateManager.getApplicationDefinition(applicationId);
		VirtualMachine vm = activeVirtueManager.getVmWithApplication(virtueId, applicationId);
		vm = activeVirtueManager.startVirtualMachine(vm);
		if (OS.LINUX.equals(vm.getOs())) {
			applicationManager.startApplicationOnVm(vm, application, params, 15);
		} else {

		}
		String hostname = vm.getHostname();
		DesktopVirtueApplication dva = new DesktopVirtueApplication(application, hostname, vm.getSshPort(),
				vm.getUserName(), vm.getPrivateKey());
		if (OS.WINDOWS.equals(dva.getOs()) && windowsPassword != null) {
			dva.setPrivateKey(windowsPassword);
		}
		logger.debug("started app: " + dva);
		return dva;
	}

	public DesktopVirtueApplication startApplicationFromTemplate(String templateId, String applicationId) {
		logger.debug("Attempting to start application from template.  TemplateId=" + templateId + " applicationId="
				+ applicationId);
		long a = System.currentTimeMillis();
		verifyAndReturnUser();
		VirtueInstance instance = createVirtue(templateId);
		while (!VirtueState.RUNNING.equals(instance.getState())) {
			JavaUtil.sleepAndLogInterruption(2000);
			instance = activeVirtueManager.getActiveVirtue(instance.getId());
			VirtueState s = instance.getState();
			if (VirtueState.RUNNING.equals(s)) {
				break;
			}
			if (VirtueState.CREATING.equals(s) || VirtueState.LAUNCHING.equals(s)) {
				continue;
			} else {
				throw new SaviorException(SaviorErrorCode.INVALID_STATE, "Error with virtue state! " + s);
			}
		}
		DesktopVirtueApplication app = startApplication(instance.getId(), applicationId, null);
		long b = System.currentTimeMillis();
		logger.debug("Starting application from template took " + ((b - a) / 1000) / 60.0 + " minutes");
		return app;
	}

	public Collection<DesktopVirtueApplication> getReconnectApps(String virtueId) {
		verifyAndReturnUser();
		VirtueInstance virtue = activeVirtueManager.getActiveVirtue(virtueId);
		ApplicationDefinition winApp = new ApplicationDefinition("reconnect", "reconnect", "1.0", OS.WINDOWS, "default",
				null);
		ApplicationDefinition linuxApp = new ApplicationDefinition("reconnect", "reconnect", "1.0", OS.LINUX, "default",
				null);
		Collection<DesktopVirtueApplication> col = new ArrayList<DesktopVirtueApplication>();
		for (VirtualMachine vm : virtue.getVms()) {
			String hostname = vm.getHostname();
			ApplicationDefinition application = (OS.LINUX.equals(vm.getOs()) ? linuxApp : winApp);
			DesktopVirtueApplication dva = new DesktopVirtueApplication(application, hostname, vm.getSshPort(),
					vm.getUserName(), vm.getPrivateKey());
			col.add(dva);
		}
		return col;
	}

	public void stopApplication(String virtueId, String applicationId) {
		verifyAndReturnUser();
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "Stop application is not yet implemented.");
	}

	public void deleteVirtue(String instanceId) {
		VirtueUser user = verifyAndReturnUser();
		activeVirtueManager.deleteVirtue(user, instanceId);
	}

	public VirtueInstance createVirtue(String templateId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueTemplate template = templateManager.getVirtueTemplateForUser(user, templateId);
		if (template == null) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_TEMPLATE_ID_NOT_FOUND,
					"Unable to find template " + templateId);
		}
		VirtueInstance instance = activeVirtueManager.provisionTemplate(user, template);
		return instance;
	}

	public DesktopVirtue startVirtue(String virtueId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueInstance instance = activeVirtueManager.startVirtue(user, virtueId);
		if (instance == null) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_ID_NOT_FOUND, "Unable to find virtue " + virtueId);
		}
		return convertVirtueInstanceToDesktopVirtue(instance);
	}

	public DesktopVirtue stopVirtue(String virtueId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueInstance instance = activeVirtueManager.stopVirtue(user, virtueId);
		if (instance == null) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_ID_NOT_FOUND, "Unable to find virtue " + virtueId);
		}
		return convertVirtueInstanceToDesktopVirtue(instance);
	}

	private DesktopVirtue convertVirtueTemplateToDesktopVirtue(VirtueTemplate template) {
		verifyAndReturnUser();
		Collection<ApplicationDefinition> apps = template.getApplications();
		Map<String, ApplicationDefinition> appsMap = new HashMap<String, ApplicationDefinition>();
		for (ApplicationDefinition app : apps) {
			appsMap.put(app.getId(), app);
		}
		return new DesktopVirtue(null, template.getName(), template.getId(), appsMap, VirtueState.UNPROVISIONED);
	}

	private DesktopVirtue convertVirtueInstanceToDesktopVirtue(VirtueInstance instance) {
		verifyAndReturnUser();
		Collection<ApplicationDefinition> apps = instance.getApplications();
		Map<String, ApplicationDefinition> appsMap = new HashMap<String, ApplicationDefinition>();
		for (ApplicationDefinition app : apps) {
			appsMap.put(app.getId(), app);
		}
		return new DesktopVirtue(instance.getId(), instance.getName(), instance.getTemplateId(), appsMap,
				instance.getState());
	}

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains(VirtueUser.ROLE_USER)) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED, "User did not have USER role");
		}
		return user;
	}

	public DesktopVirtue createVirtueAsDesktopVirtue(String templateId) {
		VirtueInstance instance = createVirtue(templateId);
		DesktopVirtue dv = convertVirtueInstanceToDesktopVirtue(instance);
		return dv;
	}

	public IconModel getIcon(String iconKey) {
		verifyAndReturnUser();
		IconModel icon = templateManager.getIcon(iconKey);
		if (icon == null) {
			icon = templateManager.getIcon(AdminService.DEFAULT_ICON_KEY);
		}
		return icon;
	}

	public DesktopVirtue terminateVirtue(String id) {
		VirtueUser user = verifyAndReturnUser();
		VirtueInstance instance = activeVirtueManager.deleteVirtue(user, id);
		return convertVirtueInstanceToDesktopVirtue(instance);
	}
}
