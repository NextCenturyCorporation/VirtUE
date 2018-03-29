package com.ncc.savior.virtueadmin.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Test and bootstrapping endpoint. This needs to be removed before production
 * deployment.
 *
 */

@Path("/data")
public class DataResource {
	private static final Logger logger = LoggerFactory.getLogger(DataResource.class);
	@Autowired
	private ITemplateManager templateManager;

	@Autowired
	private IUserManager userManager;

	@Qualifier("virtueDao")
	@Autowired
	private IActiveVirtueDao activeVirtueDao;

	@Autowired
	private ICloudManager cloudManager;

	@Autowired
	private SessionRegistry sessionRegistry;

	public DataResource() {
		logger.warn("***Data Resource is currently enabled.  Please disable for production systems.***");
	}

	@GET
	@Path("templates/preload")
	public Response preloadTemplates() {
		logger.info("attempting to preload data");
		ApplicationDefinition linuxChrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome", "1.0",
				OS.LINUX, "google-chrome");
		ApplicationDefinition linuxFirefox = new ApplicationDefinition(UUID.randomUUID().toString(), "Firefox", "1.0",
				OS.LINUX, "firefox");
		ApplicationDefinition calculator = new ApplicationDefinition(UUID.randomUUID().toString(), "Calculator", "1.0",
				OS.LINUX, "gnome-calculator");
		ApplicationDefinition lowriter = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Writer",
				"1.0", OS.LINUX, "lowriter");
		ApplicationDefinition localc = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Calc",
				"1.0", OS.LINUX, "localc");
		ApplicationDefinition lodraw = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Draw",
				"1.0", OS.LINUX, "lodraw");
		ApplicationDefinition loimpress = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Impress",
				"1.0", OS.LINUX, "loimpress");
		ApplicationDefinition linuxTerminal = new ApplicationDefinition(UUID.randomUUID().toString(), "Terminal", "1.0",
				OS.LINUX, "gnome-terminal");
		ApplicationDefinition thunderBird = new ApplicationDefinition(UUID.randomUUID().toString(), "Thunderbird",
				"1.0", OS.LINUX, "thunderbird");

		ApplicationDefinition windowsChrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome", "1.0",
				OS.WINDOWS, "google-chrome");
		ApplicationDefinition windowsFirefox = new ApplicationDefinition(UUID.randomUUID().toString(), "Firefox", "1.0",
				OS.WINDOWS, "firefox");
		// ApplicationDefinition gedit = new
		// ApplicationDefinition(UUID.randomUUID().toString(), "GEdit", "1.0", OS.LINUX,
		// "gedit");
		// ApplicationDefinition eclipse = new
		// ApplicationDefinition(UUID.randomUUID().toString(), "Eclipse", "1.0",
		// OS.LINUX, "eclipse");

		Collection<ApplicationDefinition> appsAll = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsAllLinux = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsBrowsersLinux = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsBrowsersWindows = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsMath = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsLibreOffice = new LinkedList<ApplicationDefinition>();
		// Collection<ApplicationDefinition> appsEmail = new
		// LinkedList<ApplicationDefinition>();

		appsBrowsersLinux.add(linuxChrome);
		appsBrowsersLinux.add(linuxFirefox);
		appsLibreOffice.add(localc);
		appsLibreOffice.add(lodraw);
		appsLibreOffice.add(loimpress);
		appsLibreOffice.add(lowriter);
		appsMath.add(calculator);
		appsBrowsersWindows.add(windowsChrome);
		appsBrowsersWindows.add(windowsFirefox);

		appsAll.addAll(appsBrowsersLinux);
		appsAll.addAll(appsLibreOffice);
		appsAll.addAll(appsMath);
		appsAll.addAll(appsBrowsersWindows);
		appsAll.add(linuxTerminal);
		appsAll.add(thunderBird);

		Date now = new Date();
		String systemName = "system";
		String allLinuxAmi = "ami-2b500951";
		String windowsAmi = "ami-36a65f4b";
		String linuxLoginUser = "admin";
		String windowsLoginUser = "administrator";
		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Browsers",
				OS.LINUX, allLinuxAmi, appsBrowsersLinux, linuxLoginUser, true, now, systemName);

		VirtualMachineTemplate windowsBrowserVm = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Windows",
				OS.WINDOWS, windowsAmi, appsBrowsersWindows, windowsLoginUser, true, now, systemName);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "All", OS.LINUX,
				allLinuxAmi, appsAllLinux, linuxLoginUser, true, now, systemName);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Math", OS.LINUX,
				allLinuxAmi, appsMath, linuxLoginUser, true, now, systemName);

		// VirtualMachineTemplate vmDrawing = new
		// VirtualMachineTemplate(UUID.randomUUID().toString(), "Drawing", OS.LINUX,
		// allLinuxAmi, appsDrawing, linuxLoginUser, true, now, systemName);

		VirtualMachineTemplate vmLibreOffice = new VirtualMachineTemplate(UUID.randomUUID().toString(), "LibreOffice",
				OS.LINUX, allLinuxAmi, appsLibreOffice, linuxLoginUser, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsSingleAll = new HashSet<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		// vmtsSingleAll.add(windowsVm);
		String allTemplate = "default-template";
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Test Virtue", "1.0",
				vmtsSingleAll, allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsLinuxAndWinBrowsers = new HashSet<VirtualMachineTemplate>();
		vmtsLinuxAndWinBrowsers.add(vmBrowser);
		vmtsLinuxAndWinBrowsers.add(windowsBrowserVm);
		VirtueTemplate virtueBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Web Virtue (Both OS)", "1.0",
				vmtsLinuxAndWinBrowsers, allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsLibre = new HashSet<VirtualMachineTemplate>();
		vmtsLibre.add(vmLibreOffice);
		VirtueTemplate virtueLibre = new VirtueTemplate(UUID.randomUUID().toString(), "Office Virtue", "1.0", vmtsLibre,
				allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsWindows = new HashSet<VirtualMachineTemplate>();
		vmtsWindows.add(windowsBrowserVm);
		VirtueTemplate virtueWindows = new VirtueTemplate(UUID.randomUUID().toString(), "Windows Virtue", "1.0",
				vmtsWindows, allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsMath = new HashSet<VirtualMachineTemplate>();
		vmtsMath.add(vmMath);
		VirtueTemplate virtueMath = new VirtueTemplate(UUID.randomUUID().toString(), "Math Virtue", "1.0", vmtsMath,
				allTemplate, true, now, systemName);

		for (ApplicationDefinition app : appsAll) {
			templateManager.addApplicationDefinition(app);
		}

		templateManager.addVmTemplate(vmMath);
		templateManager.addVmTemplate(vmBrowser);
		templateManager.addVmTemplate(vmLibreOffice);
		// templateManager.addVmTemplate(vmDrawing);
		templateManager.addVmTemplate(vmAll);
		templateManager.addVmTemplate(windowsBrowserVm);

		templateManager.addVirtueTemplate(virtueBrowsers);
		templateManager.addVirtueTemplate(virtueSingleAll);
		templateManager.addVirtueTemplate(virtueWindows);
		templateManager.addVirtueTemplate(virtueLibre);
		templateManager.addVirtueTemplate(virtueMath);

		ArrayList<String> userRoles = new ArrayList<String>();
		userRoles.add("ROLE_USER");
		ArrayList<String> adminRoles = new ArrayList<String>();
		adminRoles.add("ROLE_USER");
		adminRoles.add("ROLE_ADMIN");

		VirtueUser admin = new VirtueUser("admin", adminRoles);
		VirtueUser presenter = new VirtueUser("presenter", userRoles);
		VirtueUser office = new VirtueUser("office", userRoles);
		VirtueUser math = new VirtueUser("math", userRoles);
		VirtueUser browser = new VirtueUser("browser", userRoles);
		VirtueUser nerd = new VirtueUser("nerd", userRoles);
		VirtueUser developer = new VirtueUser("developer", userRoles);

		userManager.addUser(admin);
		userManager.addUser(presenter);
		userManager.addUser(office);
		userManager.addUser(math);
		userManager.addUser(browser);
		userManager.addUser(nerd);
		userManager.addUser(browser);
		userManager.addUser(developer);

		templateManager.assignVirtueTemplateToUser(admin, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueSingleAll.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueWindows.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueMath.getId());
		templateManager.assignVirtueTemplateToUser(presenter, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(presenter, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(office, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(math, virtueMath.getId());
		templateManager.assignVirtueTemplateToUser(browser, virtueWindows.getId());
		templateManager.assignVirtueTemplateToUser(browser, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(nerd, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(nerd, virtueMath.getId());
		templateManager.assignVirtueTemplateToUser(nerd, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(developer, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(developer, virtueMath.getId());

		logger.info("Data preloaded");
		return Response.ok().entity("success").build();
	}

	@GET
	@Path("user/{sourceUser}/{newUser}")
	public Response assignUser(@PathParam("sourceUser") String sourceUserName,
			@PathParam("newUser") String newUserName) {
		VirtueUser source = userManager.getUser(sourceUserName);
		VirtueUser newUser = userManager.getUser(newUserName);
		if (newUser == null) {
			Collection<String> auth = source.getAuthorities();
			// need this verbose code to cause the authorities fetch
			HashSet<String> newAuth = new HashSet<String>();
			for (String a : auth) {
				newAuth.add(a);
			}
			newUser = new VirtueUser(newUserName, source.getAuthorities());
			userManager.addUser(newUser);
		}
		Collection<String> ids = templateManager.getVirtueTemplateIdsForUser(source);
		for (String id : ids) {
			templateManager.assignVirtueTemplateToUser(newUser, id);
		}

		return Response.ok().entity("success").build();
	}

	@GET
	@Path("user/{sourceUser}")
	@Produces("application/json")
	public Map<String, VirtueTemplate> assignUser(@PathParam("sourceUser") String sourceUserName) {
		VirtueUser source = new VirtueUser(sourceUserName, new ArrayList<String>());
		Map<String, VirtueTemplate> ids = templateManager.getVirtueTemplatesForUser(source);
		return ids;
	}

	@GET
	@Path("template/user/")
	@Produces("application/json")
	public Collection<String> getAllUsersWithTemplates() {
		return templateManager.getUsersWithTemplate();
	}

	@GET
	@Path("user/")
	@Produces("application/json")
	public Iterable<VirtueUser> getUsers() {
		return userManager.getAllUsers();
	}

	@GET
	@Path("user/current")
	@Produces("application/json")
	public Iterable<VirtueUser> getCurrentLoggedInUsers() {
		List<Object> principals = sessionRegistry.getAllPrincipals();
		List<VirtueUser> users = new ArrayList<VirtueUser>(principals.size());
		for (Object p : principals) {
			User user = (User) p;
			ArrayList<String> auths = new ArrayList<String>();
			for (GrantedAuthority a : user.getAuthorities()) {
				auths.add(a.getAuthority());
			}
			VirtueUser u = userManager.getUser(user.getUsername());
			users.add(u);
		}
		return users;
	}

	@GET
	@Path("session")
	@Produces("application/json")
	public Map<String, List<String>> getAllSessions() {
		List<Object> principals = sessionRegistry.getAllPrincipals();
		Map<String, List<String>> sessionMap = new HashMap<String, List<String>>();
		for (Object principal : principals) {
			User user = (User) principal;
			List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
			ArrayList<String> list = new ArrayList<String>();
			for (SessionInformation s : sessions) {
				list.add(s.getSessionId());
			}
			sessionMap.put(user.getUsername(), list);
		}
		return sessionMap;
	}

	@GET
	@Path("templates/clear/")
	public String clearTemplatesDatabase() {
		templateManager.clear();
		return "database cleared.";
	}

	@GET
	@Path("active/clear/")
	public String clearActiveDatabase() {
		Iterable<VirtueInstance> all = activeVirtueDao.getAllActiveVirtues();
		for (VirtueInstance vi : all) {
			cloudManager.deleteVirtue(vi);
		}

		activeVirtueDao.clear();
		return "database cleared.";
	}

	@GET
	@Path("user/clear/")
	public String clearUsers() {
		userManager.clear();
		return "database cleared.";
	}

	@GET
	@Path("clear/")
	public String clearAll() {
		clearActiveDatabase();
		clearUsers();
		clearTemplatesDatabase();

		return "database cleared.";
	}

}
