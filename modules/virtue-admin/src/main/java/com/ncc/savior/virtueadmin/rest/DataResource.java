package com.ncc.savior.virtueadmin.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IResourceManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.persistent.PersistentStorageManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.service.AdminService;
import com.ncc.savior.virtueadmin.service.ImportExportService;
import com.ncc.savior.virtueadmin.service.PermissionService;

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

	@Autowired
	private IResourceManager resourceManager;

	@Qualifier("virtueDao")
	@Autowired
	private IActiveVirtueDao activeVirtueDao;

	@Autowired
	private ICloudManager cloudManager;

	@Autowired
	private SessionRegistry sessionRegistry;

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private PersistentStorageManager persistentStorageManager;

	@Autowired
	private PermissionService permissionService;
	private PathMatchingResourcePatternResolver resolver;

	public DataResource() {
		logger.warn("***Data Resource is currently enabled.  Please disable for production systems.***");
		this.resolver = new PathMatchingResourcePatternResolver();
	}

	/**
	 * Imports all data in the import folder on the classpath.
	 *
	 * @return
	 */
	@GET
	@Path("import/all")
	@Produces("text/plain")
	public String importAll() {
		int items = importExportService.importAll();
		return "imported " + items + " items.";
	}

	@GET
	@Path("import/user/admin")
	@Produces("application/json")
	public VirtueUser ensureAdminExists() {
		String username = "admin";
		try {
			return userManager.getUser(username);
		} catch (RuntimeException e) {
			Collection<String> authorities = new ArrayList<String>();
			authorities.add(VirtueUser.ROLE_ADMIN);
			authorities.add(VirtueUser.ROLE_USER);
			VirtueUser admin = new VirtueUser("admin", authorities, true);
			userManager.addUser(admin);
			return admin;
		}
	}

	@GET
	@Path("templates/preload")
	public Response preloadTemplates() {
		logger.info("attempting to preload data");
		try {
			loadIcons();
		} catch (Throwable t) {
			logger.warn("Failed to load icons", t);
		}
		ArrayList<String> tagsBrowser = new ArrayList<String>();
		tagsBrowser.add("BROWSER");

		ApplicationDefinition linuxChrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome (Linux)",
				"1.0", OS.LINUX, "chrome", "google-chrome", tagsBrowser);
		ApplicationDefinition linuxFirefox = new ApplicationDefinition(UUID.randomUUID().toString(), "Firefox", "1.0",
				OS.LINUX, "firefox", "firefox", tagsBrowser);
		ApplicationDefinition calculator = new ApplicationDefinition(UUID.randomUUID().toString(), "Calculator", "1.0",
				OS.LINUX, "calc", "gnome-calculator", null);
		ApplicationDefinition lowriter = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Writer",
				"1.0", OS.LINUX, "lo-writer", "lowriter", null);
		ApplicationDefinition localc = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Calc",
				"1.0", OS.LINUX, "lo-calc", "localc", null);
		ApplicationDefinition lodraw = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Draw",
				"1.0", OS.LINUX, "lo-draw", "lodraw", null);
		ApplicationDefinition loimpress = new ApplicationDefinition(UUID.randomUUID().toString(), "LibreOffice Impress",
				"1.0", OS.LINUX, "lo-impress", "loimpress", null);
		ApplicationDefinition linuxTerminal = new ApplicationDefinition(UUID.randomUUID().toString(), "Terminal", "1.0",
				OS.LINUX, "linux-terminal", "xterm", null);
		ApplicationDefinition thunderBird = new ApplicationDefinition(UUID.randomUUID().toString(), "Thunderbird",
				"1.0", OS.LINUX, "thunderbird", "thunderbird", null);

		ApplicationDefinition windowsChrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome (Win)",
				"1.0", OS.WINDOWS, "chrome", "c:\\windows\\notepad.exe", tagsBrowser);
		// "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
		ApplicationDefinition windowsEdge = new ApplicationDefinition(UUID.randomUUID().toString(), "Microsoft Edge",
				"1.0", OS.WINDOWS, "ms-edge", "c:\\windows\\notepad.exe", tagsBrowser);
		ApplicationDefinition windowsWord = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Microsoft Word 2013", "1.0", OS.WINDOWS, "ms-word", "c:\\windows\\notepad.exe", null);
		ApplicationDefinition windowsExcel = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Microsoft Excel 2013", "1.0", OS.WINDOWS, "ms-excel", "c:\\windows\\notepad.exe", null);
		ApplicationDefinition windowsOutlook = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Microsoft Outlook 2013", "1.0", OS.WINDOWS, "ms-outlook", "c:\\windows\\notepad.exe", null);
		ApplicationDefinition windowsPowershell = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Windows Powershell", "1.0", OS.WINDOWS, "ms-powershell",
				"c:\\windows\\system32\\WindowsPowerShell\\v1.0\\powershell.exe", null);
		ApplicationDefinition windowsSkype = new ApplicationDefinition(UUID.randomUUID().toString(), "Skype", "1.0",
				OS.WINDOWS, "skype", "c:\\windows\\notepad.exe", null);
		ApplicationDefinition windowsCommandTerminal = new ApplicationDefinition(UUID.randomUUID().toString(),
				"Windows Command Terminal", "1.0", OS.WINDOWS, "ms-terminal", "c:\\windows\\system32\\cmd.exe", null);

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
		String allLinuxAmi = "master";
		// String windowsAmi = "ami-36a65f4b";
		// ssh, but prior to sensing or additional apps
		// String windowsAmi = "ami-ca00afb7";
		// String windowsAmi = "ami-6f69b310";
		String windowsAmi = "ami-0145b58f0ced0e83d";
		String linuxLoginUser = "user";
		String windowsLoginUser = "virtue-admin";
		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Browsers",
				OS.LINUX, allLinuxAmi, appsBrowsersLinux, linuxLoginUser, true, now, systemName, "System", new Date());
		vmBrowser.setSecurityTag("power");
		VirtualMachineTemplate windowsBrowserVm = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Windows",
				OS.WINDOWS, windowsAmi, appsBrowsersWindows, windowsLoginUser, true, now, systemName, "System",
				new Date());
		windowsBrowserVm.setSecurityTag("power");
		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "All", OS.LINUX,
				allLinuxAmi, appsAllLinux, linuxLoginUser, true, now, systemName, "System", new Date());
		vmAll.setSecurityTag("power");
		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Math", OS.LINUX,
				allLinuxAmi, appsMath, linuxLoginUser, true, now, systemName, "System", new Date());
		vmMath.setSecurityTag("default");
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
				"Document Editor VM", OS.WINDOWS, windowsAmi, appsDocEditor, windowsLoginUser, true, now, systemName,
				"System", new Date());
		vmDocEditor.setSecurityTag("default");
		VirtualMachineTemplate vmWinCorpEmail = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Windows Corperate Email User VM", OS.WINDOWS, windowsAmi, appsWinCorpEmail, windowsLoginUser, true,
				now, systemName, "System", new Date());
		vmWinCorpEmail.setSecurityTag("email");
		VirtualMachineTemplate vmExternalInternet = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"External Internet Consumer VM", OS.WINDOWS, windowsAmi, appsExternalInternet, windowsLoginUser, true,
				now, systemName, "System", new Date());
		vmExternalInternet.setSecurityTag("power");
		VirtualMachineTemplate vmPowerUserWin = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Power User VM Windows", OS.WINDOWS, windowsAmi, appsPowerUserWin, windowsLoginUser, true, now,
				systemName, "System", new Date());
		vmPowerUserWin.setSecurityTag("power");
		VirtualMachineTemplate vmPowerUserLinux = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Power User Vm Linux", OS.LINUX, allLinuxAmi, appsPowerUserLinux, linuxLoginUser, true, now, systemName,
				"System", new Date());
		vmPowerUserLinux.setSecurityTag("power");
		VirtualMachineTemplate vmRouterAdmin = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Router Admin VM", OS.LINUX, allLinuxAmi, appsRouter, linuxLoginUser, true, now, systemName, "System",
				new Date());
		vmRouterAdmin.setSecurityTag("power");
		VirtualMachineTemplate vmLinuxCorpEmail = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"Linux Corperate Email User VM", OS.LINUX, allLinuxAmi, appsLinuxCorpEmail, linuxLoginUser, true, now,
				systemName, "System", new Date());
		vmLinuxCorpEmail.setSecurityTag("email");

		// VirtualMachineTemplate vmDrawing = new
		// VirtualMachineTemplate(UUID.randomUUID().toString(), "Drawing", OS.LINUX,
		// allLinuxAmi, appsDrawing, linuxLoginUser, true, now, systemName);

		VirtualMachineTemplate vmLibreOffice = new VirtualMachineTemplate(UUID.randomUUID().toString(), "LibreOffice",
				OS.LINUX, allLinuxAmi, appsLibreOffice, linuxLoginUser, true, now, systemName, "System", new Date());
		vmLibreOffice.setSecurityTag("default");
		Set<VirtualMachineTemplate> vmtsSingleAll = new HashSet<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		// vmtsSingleAll.add(windowsVm);
		String allTemplate = "default-template";
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Test Virtue", "1",
				vmtsSingleAll, allTemplate, "#66CDAA", true, now, systemName, "System", new Date());

		Set<VirtualMachineTemplate> vmtsLinuxAndWinBrowsers = new HashSet<VirtualMachineTemplate>();
		vmtsLinuxAndWinBrowsers.add(vmBrowser);
		vmtsLinuxAndWinBrowsers.add(windowsBrowserVm);
		VirtueTemplate virtueBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Web Virtue (Both OS)", "1",
				vmtsLinuxAndWinBrowsers, allTemplate, "#87CEEB", true, now, systemName, "System", new Date());

		Set<VirtualMachineTemplate> vmtsLibre = new HashSet<VirtualMachineTemplate>();
		vmtsLibre.add(vmLibreOffice);
		VirtueTemplate virtueLibre = new VirtueTemplate(UUID.randomUUID().toString(), "Office Virtue", "1", vmtsLibre,
				allTemplate, "#87CEEB", true, now, systemName, "System", new Date());

		Set<VirtualMachineTemplate> vmtsWindows = new HashSet<VirtualMachineTemplate>();
		vmtsWindows.add(windowsBrowserVm);
		VirtueTemplate virtueWindows = new VirtueTemplate(UUID.randomUUID().toString(), "Windows Virtue", "1",
				vmtsWindows, allTemplate, "#87CEEB", true, now, systemName, "System", new Date());

		Set<VirtualMachineTemplate> vmtsMath = new HashSet<VirtualMachineTemplate>();
		vmtsMath.add(vmMath);
		VirtueTemplate virtueMath = new VirtueTemplate(UUID.randomUUID().toString(), "Math Virtue", "1", vmtsMath,
				allTemplate, "#87CEEB", true, now, systemName, "System", new Date());

		VirtueTemplate virtueDocumentEditor = new VirtueTemplate(UUID.randomUUID().toString(), "Document Editor", "1",
				allTemplate, "#4B0082", true, now, systemName, vmDocEditor);
		virtueDocumentEditor.setUserCreatedBy("System");
		virtueDocumentEditor.setTimeCreatedAt(new Date());

		VirtueTemplate virtueWinCorpEmail = new VirtueTemplate(UUID.randomUUID().toString(),
				"Windows Corporate Email User", "1", allTemplate, "#87CEEB", true, now, systemName, vmWinCorpEmail);
		virtueWinCorpEmail.setUserCreatedBy("System");
		virtueWinCorpEmail.setTimeCreatedAt(new Date());

		VirtueTemplate virtueRouterAdmin = new VirtueTemplate(UUID.randomUUID().toString(), "Router Admin", "1",
				allTemplate, "#6A5ACD", true, now, systemName, vmRouterAdmin);
		virtueRouterAdmin.setUserCreatedBy("System");
		virtueRouterAdmin.setTimeCreatedAt(new Date());

		VirtueTemplate virtueLinuxCorporateEmailUser = new VirtueTemplate(UUID.randomUUID().toString(),
				"Linux Corporate Email User", "1", allTemplate, "#6A5ACD", true, now, systemName, vmLinuxCorpEmail);
		virtueLinuxCorporateEmailUser.setUserCreatedBy("System");
		virtueLinuxCorporateEmailUser.setTimeCreatedAt(new Date());

		VirtueTemplate virtueExternalInternet = new VirtueTemplate(UUID.randomUUID().toString(),
				"External Internet Consumer", "1", allTemplate, "#708090", true, now, systemName, vmExternalInternet);
		virtueExternalInternet.setUserCreatedBy("System");
		virtueExternalInternet.setTimeCreatedAt(new Date());

		VirtueTemplate virtuePowerUser = new VirtueTemplate(UUID.randomUUID().toString(),
				"Windows and Linux Power User", "1", allTemplate, "#6A5ACD", true, now, systemName, vmPowerUserWin,
				vmPowerUserLinux);
		virtuePowerUser.setUserCreatedBy("System");
		virtuePowerUser.setTimeCreatedAt(new Date());

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
		userRoles.add(VirtueUser.ROLE_USER);
		ArrayList<String> adminRoles = new ArrayList<String>();
		adminRoles.add(VirtueUser.ROLE_USER);
		adminRoles.add(VirtueUser.ROLE_ADMIN);

		VirtueUser admin = new VirtueUser("admin", adminRoles, true);
		VirtueUser admin2 = new VirtueUser("admin2", adminRoles, true);
		VirtueUser presenter = new VirtueUser("presenter", userRoles, true);
		VirtueUser office = new VirtueUser("office", userRoles, true);
		VirtueUser math = new VirtueUser("math", userRoles, true);
		VirtueUser browser = new VirtueUser("browser", userRoles, true);
		VirtueUser nerd = new VirtueUser("nerd", userRoles, true);
		VirtueUser developer = new VirtueUser("developer", userRoles, true);

		userManager.addUser(admin);
		userManager.addUser(admin2);
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
		templateManager.assignVirtueTemplateToUser(admin2, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueSingleAll.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueWindows.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueMath.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueDocumentEditor.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueWinCorpEmail.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueRouterAdmin.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueLinuxCorporateEmailUser.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtueExternalInternet.getId());
		templateManager.assignVirtueTemplateToUser(admin2, virtuePowerUser.getId());
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

		Printer printer3d = new Printer(UUID.randomUUID().toString(), "Makerbot 3D Printer", "127.0.0.10", "printing", true);
		Printer printerEpson = new Printer(UUID.randomUUID().toString(), "Epson 2780", "127.0.0.12", "Idle", true);
		Printer printerBrother = new Printer(UUID.randomUUID().toString(), "Brother HL 5040", "127.0.0.13", "Error: Out of paper", true);

		resourceManager.addPrinter(printer3d);
		resourceManager.addPrinter(printerEpson);
		resourceManager.addPrinter(printerBrother);


		FileSystem backupsFS = new FileSystem(UUID.randomUUID().toString(), "Backup tapes", "127.0.0.40", true, false, false, true);
		FileSystem longTermFS = new FileSystem(UUID.randomUUID().toString(), "Long-term storage", "127.0.0.41", true, false, false, true);
		FileSystem sharedFS = new FileSystem(UUID.randomUUID().toString(), "Shared files", "127.0.0.42", true, false, false, true);
		// FileSystem backupsFS = new FileSystem(UUID.randomUUID().toString(), "Backup tapes", "127.0.0.40");
		// FileSystem longTermFS = new FileSystem(UUID.randomUUID().toString(), "Long-term storage", "127.0.0.41");
		// FileSystem sharedFS = new FileSystem(UUID.randomUUID().toString(), "Shared files", "127.0.0.42");

		resourceManager.addFileSystem(backupsFS);
		resourceManager.addFileSystem(longTermFS);
		resourceManager.addFileSystem(sharedFS);

		logger.info("Data preloaded");
		return Response.ok().entity("success").build();
	}

	/**
	 * Load all the icons that are stored in the icons folder on the classpath
	 */
	private void loadIcons() {
		InputStream iconStream = DataResource.class.getClassLoader().getResourceAsStream("icons/savior.png");

		try {
			if (iconStream != null) {
				byte[] bytes = IOUtils.toByteArray(iconStream);
				templateManager.addIcon(AdminService.DEFAULT_ICON_KEY, bytes);
			}
		} catch (IOException e) {
			logger.error("Failed to load default icon");
		}

		loadIconsFromIconsFolder();
	}

	/**
	 * Load all the icons that are stored in the icons folder on the classpath
	 */
	private void loadIconsFromIconsFolder() {
		try {
			Resource[] resources = resolver.getResources("icons/**.png");
			for (Resource resource : resources) {
				try {
					byte[] bytes = IOUtils.toByteArray(resource.getInputStream());
					String name = resource.getFilename();
					name = name.substring(0, name.lastIndexOf("."));
					templateManager.addIcon(name, bytes);
				} catch (IOException e) {
					logger.error("Failed to load icon at " + resource.getDescription());
				}
			}
		} catch (IOException e) {
			logger.error("failed to load icons folder", e);
		}
	}

	@GET
	@Path("user/{sourceUser}/{newUser}")
	public Response assignUser(@PathParam("sourceUser") String sourceUserName,
			@PathParam("newUser") String newUserName) {
		VirtueUser source = userManager.getUser(sourceUserName);
		VirtueUser newUser;
		try {
			newUser = userManager.getUser(newUserName);
		} catch (SaviorException e) {
			Collection<String> auth = source.getAuthorities();
			// need this verbose code to cause the authorities fetch
			HashSet<String> newAuth = new HashSet<String>();
			for (String a : auth) {
				newAuth.add(a);
			}
			newUser = new VirtueUser(newUserName, source.getAuthorities(), true);
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
		VirtueUser source = new VirtueUser(sourceUserName, new ArrayList<String>(), true);
		Map<String, VirtueTemplate> ids = templateManager.getVirtueTemplatesForUser(source);
		return ids;
	}

	/**
	 * This doesn't appear to be used?
	 */
	@GET
	@Path("template/user")
	@Produces("application/json")
	public Collection<String> getAllUsersWithTemplates() {
		return templateManager.getUsersWithTemplate();
	}

	@GET
	@Path("user")
	@Produces("application/json")
	public Iterable<VirtueUser> getUsers() {
		return userManager.getAllUsers();
	}

	@GET
	@Path("user/{username}/enable")
	@Produces("application/json")
	public void enableUser(@PathParam("username") String username) {
		userManager.enableDisableUser(username, true);
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
	@Path("vm/status")
	@Produces("application/json")
	public Map<String, VmState> getAllVmStatus() {
		Map<String, VmState> result = new TreeMap<String, VmState>();
		activeVirtueDao.getAllVirtualMachines().forEach((VirtualMachine vm) -> {
			result.put(vm.getName(), vm.getState());
		});
		return result;
	}

	@GET
	@Path("vm")
	@Produces("application/json")
	public Iterable<VirtualMachine> getAllVms() {
		return activeVirtueDao.getAllVirtualMachines();
	}

	@GET
	@Path("vm/{id}")
	@Produces("application/json")
	public VirtualMachine getVm(@PathParam("id") String id) {
		Optional<VirtualMachine> vm = activeVirtueDao.getXenVm(id);
		if (vm.isPresent()) {
			return vm.get();
		} else {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Could not find vm with ID=" + id);
		}
	}

	@GET
	@Path("vm/reboot/{vmId}")
	@Produces("application/json")
	public void rebootVm(@PathParam("vmId") String vmId) {
		Optional<VirtualMachine> vm = activeVirtueDao.getXenVm(vmId);
		VirtualMachine vmToReboot;

		if (vm.isPresent()) {
			vmToReboot = vm.get();
		} else {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Could not find vm with ID=" + vmId);
		}

		VirtueInstance virtue = activeVirtueDao.getVirtueByVmId(vmId);

		if (virtue != null) {
			cloudManager.rebootVm(vmToReboot, virtue.getId());
		} else {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Could not find virtue with the vm ID=" + vmId);
		}
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
	@Path("templates/clear")
	public String clearTemplatesDatabase() {
		templateManager.clear();
		return "database cleared.";
	}

	@GET
	@Path("resources/clear")
	public String clearResourcesDatabase() {
		resourceManager.clear();
		return "database cleared.";
	}

	@GET
	@Path("active/clear")
	public String clearActiveDatabase() {
		Iterable<VirtueInstance> all = activeVirtueDao.getAllActiveVirtues();
		for (VirtueInstance vi : all) {
			CompletableFuture<VirtueInstance> future = new CompletableFuture<VirtueInstance>();
			cloudManager.deleteVirtue(vi, future);
			future.thenAccept((virtue) -> {
				activeVirtueDao.deleteVirtue(virtue);
			});
		}

		activeVirtueDao.clear();
		return "database cleared.";
	}

	@GET
	@Path("user/clear")
	public String clearUsers() {
		userManager.clear(false);
		return "Users cleared.";
	}

	@GET
	@Path("storage")
	@Produces("application/json")
	public Iterable<VirtuePersistentStorage> getAllStorage() {
		return persistentStorageManager.getAllPersistentStorage();
	}

	@GET
	@Path("storage/clear")
	public String clearStorage() {
		persistentStorageManager.deleteAllPersistentStorage();
		return "storage cleared";
	}

	@GET
	@Path("clear")
	public String clearAll() {
		clearActiveDatabase();
		clearUsers();
		clearTemplatesDatabase();
		clearResourcesDatabase();
		userManager.clear(true);
		return "database cleared.";
	}

	@GET
	@Path("permissions/raw")
	@Produces("application/json")
	public Iterable<ClipboardPermission> getAllRawPermissions() {
		Iterable<ClipboardPermission> p = permissionService.getAllRawPermissions();
		return p;
	}

	@GET
	@Path("permissions/computed")
	@Produces("application/json")
	public Map<Pair<String, String>, ClipboardPermissionOption> getAllComputedPermissions() {
		Iterable<VirtueTemplate> templates = templateManager.getAllVirtueTemplates();
		Collection<String> sourceIds = new ArrayList<String>();
		for (VirtueTemplate t : templates) {
			sourceIds.add(t.getId());
		}
		sourceIds.add(ClipboardPermission.DESKTOP_CLIENT_GROUP_ID);
		return permissionService.getAllPermissionsForSourcesAsMap(sourceIds);
	}

	@GET
	@Path("permission/{sourceId}/{destId}/{option}")
	public String setPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId,
			@PathParam("option") String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setClipboardPermission(sourceId, destId, option);
		return "Success, go back and refresh";
	}

	@GET
	@Path("permission/default/{option}")
	public String setServiceDefaultPermission(@PathParam("option") String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setDefaultClipboardPermission(option);
		return "Success, go back and refresh";
	}

	/**
	 * Returns a very simple HTML view of the permission set. This (along with this
	 * entire class) is intended only to be used for testing and debugging and not
	 * intended to be used in a production system.
	 *
	 * It is purely functional and ugly.
	 *
	 * @return
	 */
	@GET
	@Path("permissions/html")
	public Response getAllPermissionsHtmlView() {
		Iterable<VirtueTemplate> templates = templateManager.getAllVirtueTemplates();
		Collection<String> destIds = new ArrayList<String>();
		ArrayList<String> sourceIds = new ArrayList<String>();
		Map<String, String> idToName = new HashMap<String, String>();
		for (VirtueTemplate t : templates) {
			sourceIds.add(t.getId());
			destIds.add(t.getId());
			idToName.put(t.getId(), t.getName());
		}
		sourceIds.add(ClipboardPermission.DESKTOP_CLIENT_GROUP_ID);
		destIds.add(ClipboardPermission.DESKTOP_CLIENT_GROUP_ID);
		destIds.add(ClipboardPermission.DEFAULT_DESTINATION);
		Map<Pair<String, String>, ClipboardPermissionOption> activePermissions = permissionService
				.getAllPermissionsForSourcesAsMap(destIds);
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><table border='2'>").append("\n");

		sb.append("  <tr><th>src\\dest</th>").append("\n");
		for (String destId : destIds) {
			sb.append("    <td>").append(destId);
			sb.append("<br><b>").append(idToName.get(destId));
			sb.append("</b></td>").append("\n");
		}
		sb.append("  </tr>").append("\n");
		for (String sourceId : sourceIds) {
			sb.append("  <tr>").append("\n");
			sb.append("    <td>").append(sourceId);
			sb.append("<br><b>").append(idToName.get(sourceId));
			sb.append("</b></td>");
			for (String destId : destIds) {
				ImmutablePair<String, String> key = new ImmutablePair<String, String>(sourceId, destId);
				ClipboardPermissionOption permission = activePermissions.get(key);
				sb.append("    <td>");
				if (!destId.equals(sourceId)) {
					sb.append(permission);
					sb.append("<br>");
					for (ClipboardPermissionOption v : ClipboardPermissionOption.values()) {
						sb.append("<a href='../permission/").append(sourceId).append("/").append(destId).append("/")
								.append(v).append("'>");
						sb.append(v).append("</a><br>");
					}
				} else {

				}
				sb.append("</td>").append("\n");
			}
			sb.append("  </tr>").append("\n");
		}
		sb.append("</table>");
		sb.append("<br><br>");
		sb.append("Service Default:").append(permissionService.getDefaultClipboardPermission());
		sb.append("<br>");
		for (ClipboardPermissionOption v : ClipboardPermissionOption.values()) {
			sb.append("<a href='../permission/default/").append(v).append("'>");
			sb.append(v).append("</a><br>");
		}
		sb.append("</body></html>");
		return Response.status(200).entity(sb.toString()).build();
	}

}
