package com.ncc.savior.virtueadmin.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.images.IXenGuestImageManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
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
	private String rootClassPath;
	private ITemplateManager templateManager;
	private IUserManager userManager;
	private PathMatchingResourcePatternResolver resourceResolver;
	private IXenGuestImageManager imageManager;

	public ImportExportService(ITemplateManager templateManager, IUserManager userManager,
			IXenGuestImageManager imageManager) {
		this.jsonMapper = new ObjectMapper();
		this.templateManager = templateManager;
		this.userManager = userManager;
		this.imageManager = imageManager;
		resourceResolver = new PathMatchingResourcePatternResolver();
		this.rootClassPath = IMPORTS_LOCATION;
	}

	@Autowired
	private SecurityUserService securityService;

	private String virtueTemplateZipRoot = "virtues/";
	private String applicationDefnZipRoot = "applications/";
	private String virtualMachineTemplateZipRoot = "vms/";
	private String virtualMachineTemplateImageZipRoot = "images/";
	private String userZipRoot = "user/";

	/**
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public void exportDatabaseWithoutImages(OutputStream out) throws IOException {
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

	public void exportZippedAllUsers(OutputStream out) {
		Iterable<VirtueUser> users = userManager.getAllUsers();
		HashSet<String> includedImagePaths = new HashSet<String>();
		try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
			for (VirtueUser user : users) {
				addUserToZipStream(user, includedImagePaths, zipOut);
			}
		} catch (IOException e) {
			logger.error("Error writing export zip", e);
		}
	}

	public void exportZippedUser(String username, OutputStream out) {
		VirtueUser user = userManager.getUser(username);
		HashSet<String> includedImagePaths = new HashSet<String>();
		try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
			addUserToZipStream(user, includedImagePaths, zipOut);
		} catch (IOException e) {
			logger.error("Error writing export zip", e);
		}
	}

	public void exportZippedAllTemplates(OutputStream out) {
		Iterable<VirtueTemplate> templates = templateManager.getAllVirtueTemplates();
		HashSet<String> includedImagePaths = new HashSet<String>();
		try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
			for (VirtueTemplate template : templates) {
				addVirtueToZipStream(template, includedImagePaths, zipOut);
			}
		} catch (IOException e) {
			logger.error("Error writing export zip", e);
		}
	}

	public void exportZippedVirtueTemplate(String virtueTemplateId, OutputStream out) {
		VirtueTemplate template = templateManager.getVirtueTemplate(virtueTemplateId);
		HashSet<String> includedEntries = new HashSet<String>();
		try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
			addVirtueToZipStream(template, includedEntries, zipOut);
		} catch (IOException e) {
			logger.error("Error writing export zip", e);
		}
	}

	public void exportZippedVirtualMachineTemplate(String virtualMachineTemplateId, OutputStream out) {
		VirtualMachineTemplate template = templateManager.getVmTemplate(virtualMachineTemplateId);
		HashSet<String> includedEntries = new HashSet<String>();
		try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
			addVirtualMachineTemplateToZipStream(template, includedEntries, zipOut);
		} catch (IOException e) {
			logger.error("Error writing export zip", e);
		}
	}

	public void importZip(InputStream stream) {
		ZipEntry entry;
		try (ZipInputStream zipStream = new ZipInputStream(stream)) {
			// need to store the entrys to ensure they are added in the right order. They
			// are relatively small so this shouldn't be a memory issue.
			ArrayList<ApplicationDefinition> apps = new ArrayList<ApplicationDefinition>();
			ArrayList<VirtualMachineTemplate> vms = new ArrayList<VirtualMachineTemplate>();
			ArrayList<VirtueTemplate> vts = new ArrayList<VirtueTemplate>();
			ArrayList<VirtueUser> users = new ArrayList<VirtueUser>();
			while ((entry = zipStream.getNextEntry()) != null) {
				String name = entry.getName();
				InputStream uncloseableStream = new InputStream() {

					@Override
					public int read() throws IOException {
						return zipStream.read();
					}

					@Override
					public void close() {
						// do nothing so jsonMapper doesn't close the stream. We have to be careful to
						// close the other stream ourselves.
					}
				};
				if (entry.isDirectory()) {
					// skip
				} else if (name.contains(applicationDefnZipRoot)) {
					ApplicationDefinition app = jsonMapper.readValue(uncloseableStream, ApplicationDefinition.class);
					apps.add(app);
				} else if (name.contains(virtualMachineTemplateZipRoot)) {
					VirtualMachineTemplate vm = jsonMapper.readValue(uncloseableStream, VirtualMachineTemplate.class);
					vms.add(vm);
				} else if (name.contains(virtueTemplateZipRoot)) {
					VirtueTemplate virtue = jsonMapper.readValue(uncloseableStream, VirtueTemplate.class);
					vts.add(virtue);
				} else if (name.contains(userZipRoot)) {
					VirtueUser user = jsonMapper.readValue(uncloseableStream, VirtueUser.class);
					users.add(user);
				} else if (name.contains(virtualMachineTemplateImageZipRoot)) {
					try {
						importImage(entry, uncloseableStream);
					} catch (IOException e) {
						throw new SaviorException(SaviorErrorCode.IMAGE_IMPORT_ERROR, "Failed to import image " + entry,
								e);
					}
				}
				logger.debug("Entry: " + entry.getName() + " " + entry.isDirectory() + " " + entry.getSize());
			}
			for (ApplicationDefinition app : apps) {
				templateManager.addApplicationDefinition(app);
			}
			for (VirtualMachineTemplate vm : vms) {
				importVirtualMachineTemplateFromObject(vm);
			}
			for (VirtueTemplate vt : vts) {
				importVirtueTemplateFromObject(vt);
			}
			for (VirtueUser user : users) {
				importUserFromObject(user);
			}
		} catch (Throwable e) {
			logger.error("Error importing", e);
		}

	}

	/**
	 * returns true for success
	 * 
	 * @param entry
	 * @param uncloseableStream
	 * @return
	 * @throws IOException
	 */
	private boolean importImage(ZipEntry entry, InputStream uncloseableStream) throws IOException {
		String name = entry.getName();
		String path = name.substring(virtualMachineTemplateImageZipRoot.length());
		String extension = "";
		int dotIndex = path.lastIndexOf(".");
		if (dotIndex > -1) {
			extension = path.substring(dotIndex + 1);
			path = path.substring(0, dotIndex);
		}
		logger.debug("importing image to " + path + " of type " + extension + " " + entry.getSize());
		imageManager.storeStreamAsImage(path, extension, uncloseableStream);
		return true;
	}

	private void addUserToZipStream(VirtueUser user, HashSet<String> includedEntries, ZipOutputStream zipOut)
			throws JsonGenerationException, JsonMappingException, IOException {
		String entryName = userZipRoot + user.getUsername() + ".json";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (!includedEntries.contains(entryName)) {
			zipOut.putNextEntry(new ZipEntry(entryName));
			// we use ByteArrayOutputStream because the jsonMapper may try to close the
			// stream.
			jsonMapper.writeValue(baos, user);
			zipOut.write(baos.toByteArray());
			zipOut.closeEntry();
			includedEntries.add(entryName);
		}
		for (VirtueTemplate vt : user.getVirtueTemplates()) {
			addVirtueToZipStream(vt, includedEntries, zipOut);
		}
	}

	private void addVirtueToZipStream(VirtueTemplate template, HashSet<String> includedEntries, ZipOutputStream zipOut)
			throws IOException, JsonGenerationException, JsonMappingException {
		String entryName = virtueTemplateZipRoot + template.getId() + ".json";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (!includedEntries.contains(entryName)) {
			zipOut.putNextEntry(new ZipEntry(entryName));
			// we use ByteArrayOutputStream because the jsonMapper may try to close the
			// stream.
			jsonMapper.writeValue(baos, template);
			zipOut.write(baos.toByteArray());
			zipOut.closeEntry();
			includedEntries.add(entryName);
		}
		for (VirtualMachineTemplate vmt : template.getVmTemplates()) {
			addVirtualMachineTemplateToZipStream(vmt, includedEntries, zipOut);
		}
	}

	private void addVirtualMachineTemplateToZipStream(VirtualMachineTemplate vmt, HashSet<String> includedEntries,
			ZipOutputStream zipOut) throws IOException, JsonGenerationException, JsonMappingException {
		String entryName;
		ByteArrayOutputStream baos;
		String path = vmt.getTemplatePath();
		if (OS.WINDOWS.equals(vmt.getOs())) {
			entryName = virtualMachineTemplateImageZipRoot + path + ".qcow2";
			if (!includedEntries.contains(entryName)) {
				logger.info("Exporting Windows Images not yet supported!  Writing empty file");
				ZipEntry ze = new ZipEntry(entryName);
				zipOut.putNextEntry(ze);
				zipOut.write(1);
				zipOut.closeEntry();
				includedEntries.add(entryName);
			}
		} else {
			entryName = virtualMachineTemplateImageZipRoot + path + ".qcow2";
			if (!includedEntries.contains(entryName)) {
				// S3 implementation has rules about how the stream is used.
				ZipEntry ze = new ZipEntry(virtualMachineTemplateImageZipRoot + path + ".qcow2");
				zipOut.putNextEntry(ze);
				imageManager.pushImageToStream(path, zipOut);
				zipOut.closeEntry();
				includedEntries.add(entryName);
			}
		}
		entryName = virtualMachineTemplateZipRoot + vmt.getId() + ".json";
		if (!includedEntries.contains(entryName)) {
			baos = new ByteArrayOutputStream();
			zipOut.putNextEntry(new ZipEntry(entryName));
			jsonMapper.writeValue(baos, vmt);
			zipOut.write(baos.toByteArray());
			zipOut.closeEntry();
			includedEntries.add(entryName);
		}
		for (ApplicationDefinition app : vmt.getApplications()) {
			addApplicationToZipStream(app, includedEntries, zipOut);
		}
	}

	private void addApplicationToZipStream(ApplicationDefinition app, HashSet<String> includedEntries,
			ZipOutputStream zipOut) throws IOException, JsonGenerationException, JsonMappingException {
		String entryName;
		ByteArrayOutputStream baos;
		entryName = applicationDefnZipRoot + app.getId() + ".json";
		if (!includedEntries.contains(entryName)) {
			baos = new ByteArrayOutputStream();
			zipOut.putNextEntry(new ZipEntry(entryName));
			jsonMapper.writeValue(baos, app);
			zipOut.write(baos.toByteArray());
			zipOut.closeEntry();
			includedEntries.add(entryName);
		}
	}

	/**
	 * Imports the entire database. Used in conjunction with export system.
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public void importSystemDatabaseWithoutImages(InputStream stream) {
		verifyAndReturnUser();
		JsonNode node;
		try {
			node = jsonMapper.readTree(stream);
		} catch (IOException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR, "Error attempting to read json import stream", e);
		}
		// load all applications
		ArrayNode appsNode = (ArrayNode) node.get(TYPE_APPLICATION);
		for (JsonNode appNode : appsNode) {
			importApplicationFromNode(appNode);
		}
		// load all virtual machines
		ArrayNode vmtsNode = (ArrayNode) node.get(TYPE_VIRTUAL_MACHINE);
		for (JsonNode vmtNode : vmtsNode) {
			importVirtualMachineTemplateFromNode(vmtNode);
		}
		// load all virtue templates
		ArrayNode vtsNode = (ArrayNode) node.get(TYPE_VIRTUE);
		for (JsonNode vtNode : vtsNode) {
			importVirtueTemplateFromNode(vtNode);
		}
		// load all users
		ArrayNode usersNode = (ArrayNode) node.get(TYPE_USER);
		for (JsonNode userNode : usersNode) {
			importUserFromNode(userNode);
		}

	}

	private void importApplicationFromNode(JsonNode appNode) {
		try {
			ApplicationDefinition app = jsonMapper.treeToValue(appNode, ApplicationDefinition.class);
			templateManager.addApplicationDefinition(app);
		} catch (JsonProcessingException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR,
					"Error attempting to convert json node into application.  JSON=" + appNode, e);
		}
	}

	private void importVirtualMachineTemplateFromNode(JsonNode vmtNode) {
		try {
			VirtualMachineTemplate vmt = jsonMapper.treeToValue(vmtNode, VirtualMachineTemplate.class);
			// convert app Ids to Apps
			importVirtualMachineTemplateFromObject(vmt);
		} catch (JsonProcessingException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR,
					"Error attempting to convert json node into virtual machine template.  JSON=" + vmtNode, e);
		}
	}

	private void importVirtualMachineTemplateFromObject(VirtualMachineTemplate vmt) {
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

	private void importVirtueTemplateFromNode(JsonNode vtNode) {
		try {
			VirtueTemplate vt = jsonMapper.treeToValue(vtNode, VirtueTemplate.class);
			// convert vmt Ids to vmt
			importVirtueTemplateFromObject(vt);
		} catch (JsonProcessingException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR,
					"Error attempting to convert json node into virtue template.  JSON=" + vtNode, e);
		}
	}

	private void importVirtueTemplateFromObject(VirtueTemplate vt) {
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

	private void importUserFromNode(JsonNode userNode) {
		try {
			VirtueUser user = jsonMapper.treeToValue(userNode, VirtueUser.class);
			importUserFromObject(user);
		} catch (JsonProcessingException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR,
					"Error attempting to convert json node into user.  JSON=" + userNode, e);
		}
	}

	private void importUserFromObject(VirtueUser user) {
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

	/**
	 * Imports a user from a set of test users provided by APL.
	 * 
	 * @param userKey
	 * @return
	 */
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

	/**
	 * Imports a applications from a set of test applications provided by APL.
	 * 
	 * @param testApplication
	 * @return
	 */
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

	/**
	 * Imports a virtual machine from a set of test virtual machines provided by
	 * APL.
	 * 
	 * @param testVirtualMachine
	 * @return
	 */
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

	/**
	 * Imports a virtue from a set of test virtues provided by APL.
	 * 
	 * @param testVirtue
	 * @return
	 */
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
		try {
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
		} catch (IOException e) {
			logger.error("Error importing all");
		}
		return items;
	}

	private Set<String> getJsonFileNames(String type) throws IOException {
		String path = rootClassPath + "/" + type + "/**.json";
		Resource[] resources = resourceResolver.getResources(path);
		Set<String> allStrings = new HashSet<String>();
		for (Resource r : resources) {
			String name = r.getFilename();
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
		Resource resource = resourceResolver.getResource(rootClassPath + "/" + type + "/" + name);
		if (resource == null) {
			throw new SaviorException(SaviorErrorCode.IMPORT_NOT_FOUND, "Import of type=" + type + " was not found");
		}

		T instance;
		try {
			instance = jsonMapper.readValue(resource.getInputStream(), klass);
		} catch (IOException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR,
					"Unknown Error attempting to find import of type=" + type + " name=" + name);
		}
		return instance;
	}

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains(VirtueUser.ROLE_ADMIN)) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED, "User did not have ADMIN role");
		}
		return user;
	}
}
