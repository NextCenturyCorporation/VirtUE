package com.ncc.savior.virtueadmin.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.virtueadmin.data.IVirtueDataAccessObject;
import com.ncc.savior.virtueadmin.infrastructure.IInfrastructureService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

public class DesktopVirtueService {
	private IInfrastructureService infrastructure;
	private IVirtueDataAccessObject virtueDatabase;

	public DesktopVirtueService(IInfrastructureService infrastructure, IVirtueDataAccessObject virtueDatabase) {
		this.infrastructure = infrastructure;
		this.virtueDatabase = virtueDatabase;
		this.infrastructure.addStateUpdateListener(new StateUpdateListener());
	}

	public List<DesktopVirtue> getDesktopVirtuesForUser(User user) {
		return virtueDatabase.getVirtueListForUser(user);
	}

	/**
	 * Used to help with asynchronous actions
	 */
	public class StateUpdateListener implements IStateUpdateListener {
		@Override
		public void updateVirtueState(String virtueId, VirtueState state) {
			virtueDatabase.updateVirtueState(virtueId, state);
		}

		@Override
		public void updateVmState(String virtueId, String vmId, VmState state) {
			virtueDatabase.updateVmState(virtueId, vmId, state);
		}
	}

	public DesktopVirtueApplication startApplication(User user, String virtueId, String applicationId)
			throws IOException {
		VirtueInstance virtue = virtueDatabase.getVirtue(virtueId);
		return startVmAndApplication(applicationId, virtue);
	}

	public DesktopVirtueApplication startApplicationFromTemplate(User user, String templateId, String applicationId)
			throws IOException {
		VirtueTemplate template = virtueDatabase.getTemplate(templateId);
		VirtueInstance instance = infrastructure.getProvisionedVirtueFromTemplate(user, template);
		return startVmAndApplication(applicationId, instance);
	}

	private DesktopVirtueApplication startVmAndApplication(String applicationId, VirtueInstance instance)
			throws IOException {
		VirtualMachine vm = instance.findVmByApplicationId(applicationId);
		vm = infrastructure.startVm(vm);
		// TODO infrastructure.getSecurityParameters()
		SshConnectionParameters params = new SshConnectionFactory.SshConnectionParameters(vm.getHostname(),
				vm.getSshPort(), "user", "password");
		SshXpraInitiater initiator = new SshXpraInitiater(params);
		Set<Integer> displays = initiator.getXpraServers();
		int display = 45;
		if (displays.isEmpty()) {
			initiator.startXpraServer(display);
		} else {
			display = displays.iterator().next();
		}
		ApplicationDefinition app = vm.getApplications().get(applicationId);
		initiator.startXpraApp(display, app.getLaunchCommand());
		return new DesktopVirtueApplication(applicationId, app.getName(), app.getVersion(), app.getOs(),
				vm.getHostname(), vm.getSshPort());
	}
}
