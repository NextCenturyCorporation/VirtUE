package com.ncc.savior.virtueadmin.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.SecurityUserService;

/**
 * Service that manages importing and exporting data from files. The files are
 * read from classpath://imports and ./imports. The non-classpath entry
 * overrides the classpath entry for users to override the data. When importing
 * Users, Virtue Templates, or Virtual Machine Templates, the system will also
 * attempt to import dependencies. If an item or its dependency are not found,
 * an exception will be thrown.
 * 
 * Additionally, this service can import and export the entire database via
 * streams.
 * 
 *
 */
public class ImportExportService {
	private static final String IMPORTS_LOCATION = "imports";
	private static final Logger logger = LoggerFactory.getLogger(ImportExportService.class);
	private static final String IMPORT_ID_PREFIX = "IMPORT_";
	public static final String TYPE_USER = "user";
	public static final String TYPE_APPLICATION = "application";
	public static final String TYPE_VIRTUE = "virtue";
	public static final String TYPE_VIRTUAL_MACHINE = "virtualmachines";
	private ObjectMapper jsonMapper;
	private File rootClassPath;
	private ITemplateManager templateManager;
	private IUserManager userManager;
	private File rootCwd;

	public ImportExportService(ITemplateManager templateManager, IUserManager userManager) {
		this.jsonMapper = new ObjectMapper();
		this.templateManager = templateManager;
		this.userManager = userManager;
		try {
			this.rootClassPath = new ClassPathResource(IMPORTS_LOCATION).getFile();
		} catch (IOException e) {
			logger.error("Unable to initialize class path import source.  Imports may not work.");
		}
		try {
			this.rootCwd = new PathResource("./" + IMPORTS_LOCATION).getFile();
		} catch (IOException e) {
			logger.error("Unable to initialize working directory import source.  Imports may not work.");
		}
	}

	@Autowired
	private SecurityUserService securityService;

	/**
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public void exportSystem(OutputStream out) throws IOException {
		verifyAndReturnUser();
		Iterable<ApplicationDefinition> apps = templateManager.getAllApplications();
		Iterable<VirtualMachineTemplate> vmts = templateManager.getAllVirtualMachineTemplates();
		Iterable<VirtueTemplate> vts = templateManager.getAllVirtueTemplates();
		Iterable<VirtueUser> users = userManager.getAllUsers();
		@SuppressWarnings("rawtypes")
		HashMap<String, Iterable> export = new HashMap<String, Iterable>();
		export.put(TYPE_APPLICATION, apps);
		export.put(TYPE_VIRTUAL_MACHINE, vmts);
		export.put(TYPE_VIRTUE, vts);
		export.put(TYPE_USER, users);
		jsonMapper.writeValue(out, export);
	}

	public void importSystem(InputStream stream) throws IOException {
		verifyAndReturnUser();
		JsonNode node = jsonMapper.readTree(stream);
		ArrayNode appsNode = (ArrayNode) node.get(TYPE_APPLICATION);
		Iterator<JsonNode> itr = appsNode.iterator();
		while (itr.hasNext()) {
			JsonNode appNode = itr.next();
			ApplicationDefinition app = jsonMapper.treeToValue(appNode, ApplicationDefinition.class);
			templateManager.addApplicationDefinition(app);
		}

		ArrayNode vmtsNode = (ArrayNode) node.get(TYPE_VIRTUAL_MACHINE);
		itr = vmtsNode.iterator();
		while (itr.hasNext()) {
			JsonNode vmtNode = itr.next();
			VirtualMachineTemplate vmt = jsonMapper.treeToValue(vmtNode, VirtualMachineTemplate.class);
			// convert app Ids to Apps
			Collection<String> appIds = vmt.getApplicationIds();
			Iterable<ApplicationDefinition> apps = templateManager.getApplications(appIds);
			Iterator<ApplicationDefinition> ai = apps.iterator();
			ArrayList<ApplicationDefinition> myApps = new ArrayList<ApplicationDefinition>();
			while (ai.hasNext()) {
				myApps.add(ai.next());
			}
			vmt.setApplications(myApps);

			templateManager.addVmTemplate(vmt);
		}

		ArrayNode vtsNode = (ArrayNode) node.get(TYPE_VIRTUE);
		itr = vtsNode.iterator();
		while (itr.hasNext()) {
			JsonNode vtNode = itr.next();
			VirtueTemplate vt = jsonMapper.treeToValue(vtNode, VirtueTemplate.class);
			// convert vmt Ids to vmt
			Collection<String> vmtIds = vt.getVirtualMachineTemplateIds();
			Iterable<VirtualMachineTemplate> vmts = templateManager.getVmTemplates(vmtIds);
			Iterator<VirtualMachineTemplate> vmtItr = vmts.iterator();
			Set<VirtualMachineTemplate> myVmts = new HashSet<VirtualMachineTemplate>();
			while (vmtItr.hasNext()) {
				myVmts.add(vmtItr.next());
			}
			vt.setVmTemplates(myVmts);
			templateManager.addVirtueTemplate(vt);
		}

		ArrayNode usersNode = (ArrayNode) node.get(TYPE_USER);
		itr = usersNode.iterator();
		while (itr.hasNext()) {
			JsonNode userNode = itr.next();
			VirtueUser user = jsonMapper.treeToValue(userNode, VirtueUser.class);
			// convert vmt Ids to vmt
			Collection<String> vtIds = user.getVirtueTemplateIds();
			Iterable<VirtueTemplate> vts = templateManager.getVirtueTemplates(vtIds);
			Iterator<VirtueTemplate> vtItr = vts.iterator();
			Set<VirtueTemplate> myVts = new HashSet<VirtueTemplate>();
			while (vtItr.hasNext()) {
				myVts.add(vtItr.next());
			}
			user.setVirtueTemplates(myVts);
			userManager.addUser(user);
		}

	}

	public VirtueUser importUser(String userKey) {
		verifyAndReturnUser();
		VirtueUser user = read(TYPE_USER, userKey, VirtueUser.class);
		Collection<VirtueTemplate> vts = new ArrayList<VirtueTemplate>();
		// boolean exists = userManager.userExists(user.getUsername());
		// if (!exists) {
		for (String vtId : user.getVirtueTemplateIds()) {
			if (vtId.startsWith(IMPORT_ID_PREFIX)) {
				String vtKey = vtId.substring(IMPORT_ID_PREFIX.length(), vtId.length());
				VirtueTemplate vt = importVirtueTemplate(vtKey);
				vts.add(vt);
			}
		}
		user.setVirtueTemplates(vts);
		userManager.addUser(user);
		// } else {
		// user = userManager.getUser(user.getUsername());
		// }
		return user;
	}

	public ApplicationDefinition importApplication(String testApplication) {
		verifyAndReturnUser();
		ApplicationDefinition app = read(TYPE_APPLICATION, testApplication, ApplicationDefinition.class);
		String id = IMPORT_ID_PREFIX + testApplication;
		app.setId(id);
		// boolean exists = templateManager.containsApplication(id);
		// if (!exists) {
		templateManager.addApplicationDefinition(app);
		// } else {
		// app = templateManager.getApplicationDefinition(id);
		// }
		return app;
	}

	public VirtualMachineTemplate importVirtualMachineTemplate(String testVirtualMachine) {
		VirtueUser user = verifyAndReturnUser();
		VirtualMachineTemplate vmt = read(TYPE_VIRTUAL_MACHINE, testVirtualMachine, VirtualMachineTemplate.class);
		String id = IMPORT_ID_PREFIX + testVirtualMachine;
		vmt.setId(id);
		Collection<ApplicationDefinition> applications = new ArrayList<ApplicationDefinition>();
		// boolean exists = templateManager.containsVirtualMachineTemplate(id);
		// if (!exists) {
		for (String appId : vmt.getApplicationIds()) {
			if (appId.startsWith(IMPORT_ID_PREFIX)) {
				String appKey = appId.substring(IMPORT_ID_PREFIX.length(), appId.length());
				ApplicationDefinition app = importApplication(appKey);
				applications.add(app);
			}
		}
		vmt.setApplications(applications);
		vmt.setLastEditor(user.getUsername());
		vmt.setLastModification(new Date());
		templateManager.addVmTemplate(vmt);
		// } else {
		// vmt = templateManager.getVmTemplate(id);
		// }
		return vmt;
	}

	public VirtueTemplate importVirtueTemplate(String testVirtue) {
		VirtueUser user = verifyAndReturnUser();
		VirtueTemplate vt = read(TYPE_VIRTUE, testVirtue, VirtueTemplate.class);
		String id = IMPORT_ID_PREFIX + testVirtue;
		vt.setId(id);
		Collection<VirtualMachineTemplate> vmts = new ArrayList<VirtualMachineTemplate>();
		// boolean exists = templateManager.containsVirtueTemplate(id);
		// if (!exists) {
		for (String vmtId : vt.getVirtualMachineTemplateIds()) {
			if (vmtId.startsWith(IMPORT_ID_PREFIX)) {
				String vmtKey = vmtId.substring(IMPORT_ID_PREFIX.length(), vmtId.length());
				VirtualMachineTemplate vmt = importVirtualMachineTemplate(vmtKey);
				vmts.add(vmt);
			}
		}
		vt.setVmTemplates(vmts);
		vt.setLastEditor(user.getUsername());
		vt.setLastModification(new Date());
		templateManager.addVirtueTemplate(vt);
		// } else {
		// vt = templateManager.getVirtueTemplate(id);
		// }
		return vt;
	}

	/**
	 * Imports all data that can be found in the import repository files. For an
	 * item to be imported, all of its dependencies need to be importable as well.
	 * 
	 * @return
	 */
	public int importAll() {
		int items = 0;
		verifyAndReturnUser();
		for (String name : getJsonFileNames(TYPE_APPLICATION)) {
			try {
				importApplication(name);
				items++;
			} catch (Throwable t) {
				logger.error("Error trying to import application with name=" + name, t);
			}
		}
		for (String name : getJsonFileNames(TYPE_VIRTUAL_MACHINE)) {
			try {
				importVirtualMachineTemplate(name);
				items++;
			} catch (Throwable t) {
				logger.error("Error trying to import virtual machine template with name=" + name, t);
			}
		}
		for (String name : getJsonFileNames(TYPE_VIRTUE)) {
			try {
				importVirtueTemplate(name);
				items++;
			} catch (Throwable t) {
				logger.error("Error trying to import virtue template with name=" + name, t);
			}
		}
		for (String name : getJsonFileNames(TYPE_USER)) {
			try {
				importUser(name);
				items++;
			} catch (Throwable t) {
				logger.error("Error trying to import user with name=" + name, t);
			}
		}
		return items;
	}

	private Set<String> getJsonFileNames(String type) {
		File dir1 = new File(rootClassPath, type);
		File dir2 = new File(rootCwd, type);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json") || name.endsWith(".JSON");
			}
		};
		Set<File> allFiles = new HashSet<File>();
		File[] files;
		if (dir1.exists()) {
			files = dir1.listFiles(filter);
			Collections.addAll(allFiles, files);
		}
		if (dir2.exists()) {
			files = dir2.listFiles(filter);
			Collections.addAll(allFiles, files);
		}
		Set<String> allStrings = new HashSet<String>();
		for (File f : allFiles) {
			String name = f.getName();
			int dot = name.lastIndexOf(".");
			if (dot >= 0) {
				name = name.substring(0, dot);
			}
			allStrings.add(name);
		}
		return allStrings;
	}

	private <T> T read(String type, String name, Class<T> klass) {
		name = name + ".json";
		File dir = new File(rootCwd, type);
		File file = new File(dir, name);
		if (!file.exists()) {
			dir = new File(rootClassPath, type);
			file = new File(dir, name);
			if (!file.exists()) {
				throw new SaviorException(SaviorException.IMPORT_NOT_FOUND,
						"Import of type=" + type + " was not found");
			}
		}
		T instance;
		try {
			instance = jsonMapper.readValue(file, klass);
		} catch (IOException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR,
					"Unknown Error attempting to find import of type=" + type + " name=" + name);
		}
		return instance;
	}

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_ADMIN")) {
			throw new SaviorException(SaviorException.USER_NOT_AUTHORIZED, "User did not have ADMIN role");
		}
		return user;
	}
}
