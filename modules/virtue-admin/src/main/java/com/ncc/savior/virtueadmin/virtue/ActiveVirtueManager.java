package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IStateUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Implementation of {@link IActiveVirtueManager}.
 * 
 * See interface for more descriptions.
 * 
 *
 */
public class ActiveVirtueManager implements IActiveVirtueManager {

	private IVmManager vmManager;
	private IActiveVirtueDao virtueDao;

	public ActiveVirtueManager(IVmManager vmManager, IActiveVirtueDao virtueDao) {
		this.vmManager = vmManager;
		vmManager.addStateUpdateListener(new VmUpdateListener());
		this.virtueDao = virtueDao;
	}

	@Override
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(User user, Set<String> templateIds) {
		Map<String, Set<VirtueInstance>> virtues = virtueDao.getVirtuesFromTemplateIds(user, templateIds);
		return virtues;
	}

	@Override
	public VirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		VirtualMachine vm = virtueDao.getVmWithApplication(virtueId, applicationId);
		return vm;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		return vmManager.startVirtualMachine(vm);
	}

	@Override
	public VirtueInstance provisionTemplate(User user, VirtueTemplate template) {
		Collection<VirtualMachineTemplate> vmTemplates = template.getVmTemplates();
		Collection<VirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(vmTemplates);
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		virtueDao.addVirtue(vi);
		return vi;
	}

	protected void updateVmState(String vmId, VmState state) {
		virtueDao.updateVmState(vmId, state);
	}

	private class VmUpdateListener implements IStateUpdateListener {
		@Override
		public void updateVmState(String vmId, VmState state) {
			updateVmState(vmId, state);
		}
	}
}
