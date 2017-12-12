package com.ncc.savior.virtueadmin.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

		Collection<ApplicationDefinition> appsAll = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsBrowsers = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appsMath = new LinkedList<ApplicationDefinition>();
		Collection<ApplicationDefinition> appChromeIsBetterThanFirefox = new LinkedList<ApplicationDefinition>();

		appsAll.add(chrome);
		appsAll.add(firefox);
		appsAll.add(calculator);
		appsBrowsers.add(chrome);
		appsBrowsers.add(firefox);
		appChromeIsBetterThanFirefox.add(chrome);
		appChromeIsBetterThanFirefox.add(calculator);
		appsMath.add(calculator);

		VirtualMachineTemplate vmBrowser = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Browsers",
				OS.LINUX, "Linux Browsers", appsBrowsers);

		VirtualMachineTemplate vmAll = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux All", OS.LINUX,
				"Linux All", appsAll);

		VirtualMachineTemplate vmMath = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Linux Math", OS.LINUX,
				"Linux Math", appsMath);

		List<VirtualMachineTemplate> vmtsSingleAll = new ArrayList<VirtualMachineTemplate>();
		vmtsSingleAll.add(vmAll);
		VirtueTemplate virtueSingleAll = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Single VM Virtue",
				"1.0", vmtsSingleAll);
		List<VirtualMachineTemplate> vmtsBrowsers = new ArrayList<VirtualMachineTemplate>();
		vmtsBrowsers.add(vmBrowser);
		VirtueTemplate virtueSingleBrowsers = new VirtueTemplate(UUID.randomUUID().toString(), "Linux Browser Virtue",
				"1.0", vmtsBrowsers);
		List<VirtualMachineTemplate> vmts = new ArrayList<VirtualMachineTemplate>();
		vmts.add(vmBrowser);
		vmts.add(vmAll);
		vmts.add(vmMath);
		VirtueTemplate virtueAllVms = new VirtueTemplate(UUID.randomUUID().toString(), "Linux All VMs Virtue", "1.0",
				vmts);

		templateManager.addApplicationDefinition(calculator);
		templateManager.addApplicationDefinition(firefox);
		templateManager.addApplicationDefinition(chrome);
		//
		// templateManager.addVmTemplate(vmAll);
		templateManager.addVmTemplate(vmMath);
		templateManager.addVmTemplate(vmBrowser);

		templateManager.addVirtueTemplate(virtueSingleBrowsers);
		// templateManager.addVirtueTemplate(virtueSingleAll);

		User kdrumm = new User("kdrumm");
		User wole = new User("wole");
		templateManager.assignVirtueTemplateToUser(User.testUser(), virtueSingleBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(kdrumm, virtueSingleBrowsers.getId());
		templateManager.assignVirtueTemplateToUser(wole, virtueSingleBrowsers.getId());

		templateManager.addVmTemplate(vmAll);
		templateManager.addVirtueTemplate(virtueSingleAll);
		templateManager.addVirtueTemplate(virtueAllVms);

		templateManager.assignVirtueTemplateToUser(User.testUser(), virtueAllVms.getId());
		logger.info("Data preloaded");
		return Response.noContent().entity("success").build();
	}
}
