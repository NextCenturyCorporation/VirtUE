package com.ncc.savior.virtueadmin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.Application;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Virtue Backend data store implementation using in-memory java data
 * structures. This should not be used in production and is for testing only.
 * 
 *
 */
public class InMemoryVirtueDao implements IVirtueDataAccessObject {

	private HashMap<String, List<VirtueTemplate>> userToTemplates;
	private HashMap<String, List<VirtueInstance>> userToActiveVirtues;
	private HashMap<String, VirtueTemplate> templateIdToTemplate;
	private HashMap<String, VirtueInstance> virtueIdToVirtue;
	private HashMap<String, Application> applicationIdToApplication;

	public InMemoryVirtueDao() {
		readDbFile();
	}

	private void initiateNewTestDb() {
		userToTemplates = new HashMap<String, List<VirtueTemplate>>();
		userToActiveVirtues = new HashMap<String, List<VirtueInstance>>();
		templateIdToTemplate = new HashMap<String, VirtueTemplate>();
		virtueIdToVirtue = new HashMap<String, VirtueInstance>();
		applicationIdToApplication = new HashMap<String, Application>();

		Application chrome = new Application(UUID.randomUUID().toString(), "Chrome", "1.0", OS.LINUX, "google-chrome");
		Application firefox = new Application(UUID.randomUUID().toString(), "Firefox", "1.0", OS.LINUX, "firefox");
		Application calculator = new Application(UUID.randomUUID().toString(), "Calculator", "1.0", OS.LINUX,
				"gnome-calculator");

		Set<String> applicationIds = new HashSet<String>();
		applicationIdToApplication.put(chrome.getId(), chrome);
		applicationIdToApplication.put(firefox.getId(), firefox);
		applicationIdToApplication.put(calculator.getId(), calculator);
		applicationIds.add(chrome.getId());
		applicationIds.add(firefox.getId());
		applicationIds.add(calculator.getId());
		VirtueTemplate template = new VirtueTemplate(UUID.randomUUID().toString(), "default", "1.0", applicationIds);
		templateIdToTemplate.put(template.getId(), template);

		List<VirtueTemplate> templates = new ArrayList<VirtueTemplate>();
		templates.add(template);
		User user = User.testUser();
		userToTemplates.put(user.getUsername(), templates);
	}

	private void readDbFile() {
		// TODO check for file and if not exists, initiate
		initiateNewTestDb();
	}

	@Override
	public List<VirtueTemplate> getTemplatesForUser(User user) {
		List<VirtueTemplate> templates = userToTemplates.get(user.getUsername());
		if (templates == null) {
			return new ArrayList<VirtueTemplate>(0);
		} else {
			return new ArrayList<VirtueTemplate>(templates);
		}
	}

	@Override
	public List<VirtueInstance> getVirtuesForUser(User user) {
		List<VirtueInstance> virtues = userToActiveVirtues.get(user.getUsername());
		if (virtues == null) {
			return new ArrayList<VirtueInstance>(0);
		} else {
			return new ArrayList<VirtueInstance>(virtues);
		}
	}

	@Override
	public VirtueTemplate getTemplate(String templateId) {
		return templateIdToTemplate.get(templateId);
	}

	@Override
	public VirtueInstance getVirtue(String virtueId) {
		return virtueIdToVirtue.get(virtueId);
	}

	@Override
	public List<Application> getApplicationsForVirtue(User user, String virtueId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addVirtueForUser(User user, VirtueInstance virtue) {
		// TODO synchronization?
		virtueIdToVirtue.put(virtue.getId(), virtue);

		List<VirtueInstance> virtues = userToActiveVirtues.get(user.getUsername());
		if (virtues == null) {
			virtues = new ArrayList<VirtueInstance>();
			userToActiveVirtues.put(user.getUsername(), virtues);
		}
		virtues.add(virtue);
	}

}
