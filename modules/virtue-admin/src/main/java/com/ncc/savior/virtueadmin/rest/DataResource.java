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
		ApplicationDefinition linuxChrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome (Linux)",
				"1.0",
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
				OS.LINUX, "xterm");
		ApplicationDefinition thunderBird = new ApplicationDefinition(UUID.randomUUID().toString(), "Thunderbird",
				"1.0", OS.LINUX, "thunderbird");

		ApplicationDefinition windowsChrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome (Win)",
				"1.0", OS.WINDOWS, "c:\\windows\\notepad.exe");
		ApplicationDefinition windowsEdge = new ApplicationDefinition(UUID.randomUUID().toString(), "Microsoft Edge",
				"1.0", OS.WINDOWS, "c:\\windows\\notepad.exe");
		ApplicationDefinition windowsWord = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Microsoft Word 2013", "1.0", OS.WINDOWS, "c:\\windows\\notepad.exe");
		ApplicationDefinition windowsExcel = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Microsoft Excel 2013", "1.0", OS.WINDOWS, "c:\\windows\\notepad.exe");
		ApplicationDefinition windowsOutlook = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Microsoft Outlook 2013", "1.0", OS.WINDOWS, "c:\\windows\\notepad.exe");
		ApplicationDefinition windowsPowershell = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Windows Powershell", "1.0", OS.WINDOWS,
				"c:\\windows\\system32\\WindowsPowerShell\\v1.0\\powershell.exe");
		ApplicationDefinition windowsSkype = new ApplicationDefinition(UUID.randomUUID().toString(), "Skype", "1.0",
				OS.WINDOWS, "c:\\windows\\notepad.exe");
		ApplicationDefinition windowsCommandTerminal = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Windows Command Terminal", "1.0", OS.WINDOWS, "c:\\windows\\system32\\cmd.exe");

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
		appsBrowsersWindows.add(windowsEdge);

		appsAllLinux.addAll(appsBrowsersLinux);
		appsAllLinux.addAll(appsLibreOffice);
		appsAllLinux.addAll(appsMath);
		appsAllLinux.add(linuxTerminal);
		appsAllLinux.add(thunderBird);
		appsAll.addAll(appsAllLinux);
		appsAll.add(windowsChrome);
		appsAll.add(windowsEdge);
		appsAll.add(windowsWord);
		appsAll.add(windowsExcel);
		appsAll.add(windowsOutlook);
		appsAll.add(windowsPowershell);
		appsAll.add(windowsSkype);
		appsAll.add(windowsCommandTerminal);

		Date now = new Date();
		String systemName = "system";
		String allLinuxAmi = "ami-2b500951";
		String windowsAmi = "ami-36a65f4b";
		windowsAmi = "ami-ca00afb7";
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

		Collection<ApplicationDefinition> appsDocEditor = new LinkedList<ApplicationDefinition>();
		appsDocEditor.add(windowsWord);
		appsDocEditor.add(windowsExcel);
		appsDocEditor.add(windowsEdge);
		Collection<ApplicationDefinition> appsWinCorpEmail = new LinkedList<ApplicationDefinition>();
		appsWinCorpEmail.add(windowsOutlook);
		appsWinCorpEmail.add(windowsEdge);
		Collection<ApplicationDefinition> appsExternalInternet = new LinkedList<ApplicationDefinition>();
		appsExternalInternet.add(windowsChrome);
		appsExternalInternet.add(windowsSkype);
		Collection<ApplicationDefinition> appsPowerUserWin = new LinkedList<ApplicationDefinition>();
		appsPowerUserWin.add(windowsCommandTerminal);
		appsPowerUserWin.add(windowsPowershell);
		Collection<ApplicationDefinition> appsPowerUserLinux = new LinkedList<ApplicationDefinition>();
		appsPowerUserLinux.add(linuxChrome);
		appsPowerUserLinux.add(linuxTerminal);
		Collection<ApplicationDefinition> appsRouter = new LinkedList<ApplicationDefinition>();
		appsRouter.add(linuxTerminal);
		appsRouter.add(linuxFirefox);
		Collection<ApplicationDefinition> appsLinuxCorpEmail = new LinkedList<ApplicationDefinition>();
		appsLinuxCorpEmail.add(thunderBird);
		appsLinuxCorpEmail.add(linuxFirefox);
		VirtualMachineTemplate vmDocEditor = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Document Editor VM", OS.WINDOWS, windowsAmi, appsDocEditor, windowsLoginUser, true, now, systemName);
		VirtualMachineTemplate vmWinCorpEmail = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Windows Corperate Email User VM", OS.WINDOWS, windowsAmi, appsWinCorpEmail, windowsLoginUser, true,
				now, systemName);
		VirtualMachineTemplate vmExternalInternet = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"External Internet Consumer VM", OS.WINDOWS, windowsAmi, appsExternalInternet, windowsLoginUser, true,
				now, systemName);
		VirtualMachineTemplate vmPowerUserWin = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Power User VM Windows", OS.WINDOWS, windowsAmi, appsPowerUserWin, windowsLoginUser, true, now,
				systemName);
		VirtualMachineTemplate vmPowerUserLinux = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Power User Vm Linux", OS.LINUX, allLinuxAmi, appsPowerUserLinux, linuxLoginUser, true, now,
				systemName);
		VirtualMachineTemplate vmRouterAdmin = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Router Admin VM", OS.LINUX, allLinuxAmi, appsRouter, linuxLoginUser, true, now, systemName);
		VirtualMachineTemplate vmLinuxCorpEmail = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Linux Corperate Email User VM", OS.LINUX, allLinuxAmi, appsLinuxCorpEmail, linuxLoginUser, true, now,
				systemName);

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

		VirtueTemplate virtueDocumentEditor = new VirtueTemplate(UUID.randomUUID().toString(), "Document Editor", "1.0",
				allTemplate, true, now, systemName, vmDocEditor);
		VirtueTemplate virtueWinCorpEmail = new VirtueTemplate(UUID.randomUUID().toString(),
				"Windows Corporate Email User", "1.0", allTemplate, true, now, systemName, vmWinCorpEmail);
		VirtueTemplate virtueRouterAdmin = new VirtueTemplate(UUID.randomUUID().toString(), "Router Admin", "1.0",
				allTemplate, true, now, systemName, vmRouterAdmin);
		VirtueTemplate virtueLinuxCorporateEmailUser = new VirtueTemplate(UUID.randomUUID().toString(),
				"Linux Corporate Email User", "1.0", allTemplate, true, now, systemName, vmLinuxCorpEmail);
		VirtueTemplate virtueExternalInternet = new VirtueTemplate(UUID.randomUUID().toString(),
				"Enternal Internet Consumer", "1.0", allTemplate, true, now, systemName, vmExternalInternet);
		VirtueTemplate virtuePowerUser = new VirtueTemplate(UUID.randomUUID().toString(),
				"Windows and Linux Power User", "1.0", allTemplate, true, now, systemName, vmPowerUserWin,
				vmPowerUserLinux);

		for (ApplicationDefinition app : appsAll) {
			templateManager.addApplicationDefinition(app);
		}

		templateManager.addVmTemplate(vmMath);
		templateManager.addVmTemplate(vmBrowser);
		templateManager.addVmTemplate(vmLibreOffice);
		// templateManager.addVmTemplate(vmDrawing);
		templateManager.addVmTemplate(vmAll);
		templateManager.addVmTemplate(windowsBrowserVm);

		templateManager.addVmTemplate(vmDocEditor);
		templateManager.addVmTemplate(vmWinCorpEmail);
		templateManager.addVmTemplate(vmRouterAdmin);
		templateManager.addVmTemplate(vmLinuxCorpEmail);
		templateManager.addVmTemplate(vmExternalInternet);
		templateManager.addVmTemplate(vmPowerUserWin);
		templateManager.addVmTemplate(vmPowerUserLinux);

		templateManager.addVirtueTemplate(virtueBrowsers);
		templateManager.addVirtueTemplate(virtueSingleAll);
		templateManager.addVirtueTemplate(virtueWindows);
		templateManager.addVirtueTemplate(virtueLibre);
		templateManager.addVirtueTemplate(virtueMath);

		templateManager.addVirtueTemplate(virtueDocumentEditor);
		templateManager.addVirtueTemplate(virtueWinCorpEmail);
		templateManager.addVirtueTemplate(virtueRouterAdmin);
		templateManager.addVirtueTemplate(virtueLinuxCorporateEmailUser);
		templateManager.addVirtueTemplate(virtueExternalInternet);
		templateManager.addVirtueTemplate(virtuePowerUser);

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
		templateManager.assignVirtueTemplateToUser(admin, virtueDocumentEditor.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueWinCorpEmail.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueRouterAdmin.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueLinuxCorporateEmailUser.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueExternalInternet.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtuePowerUser.getId());
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
