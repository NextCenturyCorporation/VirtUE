package com.ncc.savior.virtueadmin.service;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.infrastructure.AwsManager;
import com.ncc.savior.virtueadmin.infrastructure.IApplicationManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

/**
 * Virtue service to handle functions specifically for the desktop application.
 * 
 *
 */
public class DesktopVirtueService {
	private IActiveVirtueManager activeVirtueManager;
	private ITemplateManager templateManager;
	private IApplicationManager applicationManager;
	private AwsManager awsManager;

	public DesktopVirtueService(IActiveVirtueManager activeVirtueManager, ITemplateManager templateManager,
			IApplicationManager applicationManager, AwsManager awsManager) {
		this.activeVirtueManager = activeVirtueManager;
		this.templateManager = templateManager;
		this.applicationManager = applicationManager;
		this.awsManager = awsManager;
	}

	/**
	 * Gets all virtues as {@link DesktopVirtue}s for the user including Virtues
	 * that have not been provisioned yet, but the user has the ability to
	 * provision.
	 * 
	 * @return
	 * 
	 */
	public Set<DesktopVirtue> getDesktopVirtuesForUser(User user) {
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

	public DesktopVirtueApplication startApplication(User user, String virtueId, String applicationId)
			throws IOException {
		ApplicationDefinition application = templateManager.getApplicationDefinition(applicationId);
		VirtualMachine vm = activeVirtueManager.getVmWithApplication(virtueId, applicationId);
		vm = activeVirtueManager.startVirtualMachine(vm);
		applicationManager.startApplicationOnVm(vm, application);
		DesktopVirtueApplication dva = new DesktopVirtueApplication(application, vm.getHostname(), vm.getSshPort(),
				vm.getUserName(), vm.getPrivateKey());
		return dva;
	}

	public DesktopVirtueApplication startApplicationFromTemplate(User user, String templateId, String applicationId)
			throws IOException {
		VirtueInstance instance = createVirtue(user, templateId);
		return startApplication(user, instance.getId(), applicationId);
	}

	private DesktopVirtue convertVirtueTemplateToDesktopVirtue(VirtueTemplate template) {
		Map<String, ApplicationDefinition> apps = template.getApplications();
		// Map<String, DesktopVirtueApplication> apps = new HashMap<String,
		// DesktopVirtueApplication>();
		// for (ApplicationDefinition app : template.getApplications().values()) {
		// apps.put(app.getId(),
		// new DesktopVirtueApplication(app.getId(), app.getName(), app.getVersion(),
		// app.getOs(), null, -1));
		// }
		return new DesktopVirtue(null, template.getName(), template.getId(), apps);
	}

	private DesktopVirtue convertVirtueInstanceToDesktopVirtue(VirtueInstance instance) {
		Map<String, ApplicationDefinition> apps = instance.getApplications();
		// Map<String, DesktopVirtueApplication> apps = new HashMap<String,
		// DesktopVirtueApplication>();
		// for (ApplicationDefinition app : instance.getApplications().values()) {
		// apps.put(app.getId(), new DesktopVirtueApplication(app.getId(),
		// app.getName(), app.getVersion(),
		// app.getOs(), null, -1));
		// }
		return new DesktopVirtue(instance.getId(), instance.getName(), instance.getTemplateId(), apps);
	}


	public void deleteVirtue(User user, String instanceId) {
		activeVirtueManager.deleteVirtue(user, instanceId);
	}

	public VirtueInstance createVirtue(User user, String templateId) {
		VirtueTemplate template = templateManager.getTemplate(user, templateId);
		if (template == null) {
			throw new SaviorException(SaviorException.INVALID_TEMPATE_ID, "Unable to find template " + templateId);
		}
		VirtueInstance instance = activeVirtueManager.provisionTemplate(user, template);
		return instance;
	}
}
