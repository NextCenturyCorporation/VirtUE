package com.ncc.savior.virtueadmin.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Date;
import java.util.LinkedList;
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

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

@Path("/data")
public class DataResource {
	private static final Logger logger = LoggerFactory.getLogger(DataResource.class);
	@Autowired
	private ITemplateManager templateManager;

	@Autowired
	private IUserManager userManager;

	// TODO find where second version comes from so we can use @Autowired
	@Qualifier("virtueDao")
	@Autowired
	private IActiveVirtueDao activeVirtueDao;

	public DataResource() {
	}

	@GET
	@Path("templates/preload")
	public Response preloadTemplates() {
		logger.info("attempting to preload data");
		ApplicationDefinition chrome = new ApplicationDefinition(UUID.randomUUID().toString(), "Chrome", "1.0",
				OS.LINUX, "google-chrome");
		ApplicationDefinition firefox = new ApplicationDefinition(UUID.randomUUID().toString(), "Firefox", "1.0",
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
		ApplicationDefinition gimp = new ApplicationDefinition(UUID.randomUUID().toString(), "GIMP", "1.0", OS.LINUX,
				"gimp");
		ApplicationDefinition pinta = new ApplicationDefinition(UUID.randomUUID().toString(), "Pinta", "1.0", OS.LINUX,
				"pinta");
//		ApplicationDefinition gedit = new ApplicationDefinition(UUID.randomUUID().toString(), "GEdit", "1.0", OS.LINUX,
//				"gedit");
//		ApplicationDefinition eclipse = new ApplicationDefinition(UUID.randomUUID().toString(), "Eclipse", "1.0",
//				OS.LINUX, "eclipse");

		Collection<ApplicationDefinition> appsAll = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsBrowsers = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsMath = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsLibreOffice = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsDrawing = new LinkedList<ApplicationDefinition>();

		appsBrowsers.add(chrome);
		appsBrowsers.add(firefox);
		appsLibreOffice.add(localc);
		appsLibreOffice.add(lodraw);
		appsLibreOffice.add(loimpress);
		appsLibreOffice.add(lowriter);
		appsMath.add(calculator);
		appsDrawing.add(gimp);
		appsDrawing.add(pinta);

		appsAll.addAll(appsBrowsers);
		appsAll.addAll(appsLibreOffice);
		appsAll.addAll(appsMath);
		appsAll.addAll(appsDrawing);
		appsAll.addAll(appsMath);

		Date now = new Date();
		String systemName = "system";

		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Browsers",
				OS.LINUX, "Browsers", appsBrowsers, true, now, systemName);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "All", OS.LINUX, "All",
				appsAll, true, now, systemName);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Math", OS.LINUX,
				"Math", appsMath, true, now, systemName);

		VirtualMachineTemplate vmDrawing = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Drawing", OS.LINUX,
				"Drawing", appsDrawing, true, now, systemName);

		VirtualMachineTemplate vmLibreOffice = new VirtualMachineTemplate(UUID.randomUUID().toString(), "LibreOffice",
				OS.LINUX, "LibreOffice", appsLibreOffice, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsSingleAll = new HashSet<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		String allTemplate = "default-template";
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Test Virtue", "1.0",
				vmtsSingleAll, allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsBrowsers = new HashSet<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Web Virtue", "1.0",
				vmtsBrowsers, allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsLibre = new HashSet<VirtualMachineTemplate>();
		vmtsLibre.add(vmLibreOffice);
		VirtueTemplate virtueLibre = new VirtueTemplate(UUID.randomUUID().toString(), "Office Virtue", "1.0", vmtsLibre,
				allTemplate, true, now, systemName);

		Set<VirtualMachineTemplate> vmtsDrawing = new HashSet<VirtualMachineTemplate>();
		vmtsDrawing.add(vmDrawing);
		VirtueTemplate virtueDrawing = new VirtueTemplate(UUID.randomUUID().toString(), "Artist Virtue", "1.0",
				vmtsDrawing, allTemplate, true, now, systemName);

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
		templateManager.addVmTemplate(vmDrawing);
		templateManager.addVmTemplate(vmAll);

		templateManager.addVirtueTemplate(virtueBrowsers);
		templateManager.addVirtueTemplate(virtueSingleAll);
		templateManager.addVirtueTemplate(virtueDrawing);
		templateManager.addVirtueTemplate(virtueLibre);
		templateManager.addVirtueTemplate(virtueMath);

		ArrayList<String> userRoles = new ArrayList<String>();
		userRoles.add("ROLE_USER");
		ArrayList<String> adminRoles = new ArrayList<String>();
		adminRoles.add("ROLE_USER");
		adminRoles.add("ROLE_ADMIN");

		User admin = new User("admin", adminRoles);
		User presenter = new User("presenter", userRoles);
		User office = new User("office", userRoles);
		User math = new User("math", userRoles);
		User drawing = new User("drawing", userRoles);
		User browser = new User("browser", userRoles);
		User nerd = new User("nerd", userRoles);
		User artist = new User("artist", userRoles);
		User developer = new User("developer", userRoles);

		userManager.addUser(admin);
		userManager.addUser(presenter);
		userManager.addUser(office);
		userManager.addUser(math);
		userManager.addUser(drawing);
		userManager.addUser(browser);
		userManager.addUser(nerd);
		userManager.addUser(artist);
		userManager.addUser(developer);

		templateManager.assignVirtueTemplateToUser(admin, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueSingleAll.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueDrawing.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(admin, virtueMath.getId());
		templateManager.assignVirtueTemplateToUser(presenter, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(presenter, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(office, virtueLibre.getId());
		templateManager.assignVirtueTemplateToUser(math, virtueMath.getId());
		templateManager.assignVirtueTemplateToUser(artist, virtueDrawing.getId());
		templateManager.assignVirtueTemplateToUser(artist, virtueBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(drawing, virtueDrawing.getId());
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
		User source = userManager.getUser(sourceUserName);
		User newUser = userManager.getUser(newUserName);
		if (newUser == null) {
			Collection<String> auth = source.getAuthorities();
			//need this verbose code to cause the authorities fetch
			HashSet<String> newAuth = new HashSet<String>();
			for (String a:auth) {
				newAuth.add(a);
			}
			newUser = new User(newUserName, source.getAuthorities());
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
		User source = new User(sourceUserName, new ArrayList<String>());
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
	public Iterable<User> getUsers() {
		return userManager.getAllUsers();
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
		clearTemplatesDatabase();
		clearUsers();
		return "database cleared.";
	}

}
