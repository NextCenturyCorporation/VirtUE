package com.ncc.savior.virtueadmin.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Browsers",
				OS.LINUX, "Browsers", appsBrowsers);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "All", OS.LINUX, "All",
				appsAll);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Math", OS.LINUX,
				"Math", appsMath);

		VirtualMachineTemplate vmDrawing = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Drawing", OS.LINUX,
				"Drawing", appsDrawing);

		VirtualMachineTemplate vmLibreOffice = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"LibreOffice", OS.LINUX, "LibreOffice", appsLibreOffice);

		List<VirtualMachineTemplate> vmtsSingleAll = new ArrayList<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Single VM Virtue",
				"1.0", vmtsSingleAll);

		List<VirtualMachineTemplate> vmtsBrowsers = new ArrayList<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Browser Virtue", "1.0",
				vmtsBrowsers);

		List<VirtualMachineTemplate> vmtsLibre = new ArrayList<VirtualMachineTemplate>();
		vmtsLibre.add(vmLibreOffice);
		VirtueTemplate virtueLibre = new VirtueTemplate(UUID.randomUUID().toString(), "LibreOffice Virtue", "1.0",
				vmtsLibre);

		List<VirtualMachineTemplate> vmtsDrawing = new ArrayList<VirtualMachineTemplate>();
		vmtsDrawing.add(vmDrawing);
		VirtueTemplate virtueDrawing = new VirtueTemplate(UUID.randomUUID().toString(), "Drawing Virtue", "1.0",
				vmtsDrawing);

		List<VirtualMachineTemplate> vmtsMath = new ArrayList<VirtualMachineTemplate>();
		vmtsMath.add(vmMath);
		VirtueTemplate virtueMath = new VirtueTemplate(UUID.randomUUID().toString(), "Math Virtue", "1.0",
				vmtsMath);

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

		User admin = new User("admin", new ArrayList<String>());
		User presenter = new User("presenter", new ArrayList<String>());
		User office = new User("office", new ArrayList<String>());
		User math = new User("math", new ArrayList<String>());
		User drawing = new User("drawing", new ArrayList<String>());
		User browser = new User("browser", new ArrayList<String>());
		User nerd = new User("nerd", new ArrayList<String>());
		User artist = new User("artist", new ArrayList<String>());

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

		logger.info("Data preloaded");
		return Response.ok().entity("success").build();
	}

	@GET
	@Path("user/{sourceUser}/{newUser}")
	public Response assignUser(@PathParam("sourceUser") String sourceUserName,
			@PathParam("newUser") String newUserName) {
		User source = new User(sourceUserName, new ArrayList<String>());
		User newUser = new User(newUserName, new ArrayList<String>());
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
	@Path("user/")
	@Produces("application/json")
	public Collection<String> getUsers() {
		return templateManager.getUsers();
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

}
