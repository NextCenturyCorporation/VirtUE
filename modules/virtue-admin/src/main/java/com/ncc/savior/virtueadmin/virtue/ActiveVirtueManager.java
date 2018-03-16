package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdateListener;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;

import persistance.JpaVirtualMachine;
import persistance.JpaVirtueInstance;
import persistance.JpaVirtueTemplate;
import persistance.JpaVirtueUser;

/**
 * Implementation of {@link IActiveVirtueManager}.
 * 
 * See interface for more descriptions.
 *
 */
public class ActiveVirtueManager implements IActiveVirtueManager, IVmUpdateListener {
	private final static Logger logger = LoggerFactory.getLogger(ActiveVirtueManager.class);

	private IActiveVirtueDao virtueDao;
	private ICloudManager cloudManager;

	public ActiveVirtueManager(ICloudManager cloudManager, IActiveVirtueDao virtueDao) {
		this.cloudManager = cloudManager;
		this.virtueDao = virtueDao;
	}

	@Override
	public Map<String, Set<JpaVirtueInstance>> getVirtuesFromTemplateIds(JpaVirtueUser user, Set<String> templateIds) {
		Map<String, Set<JpaVirtueInstance>> virtues = virtueDao.getVirtuesFromTemplateIds(user, templateIds);
		return virtues;
	}

	@Override
	public Collection<JpaVirtueInstance> getVirtuesForUser(JpaVirtueUser user) {
		return virtueDao.getVirtuesForUser(user);
	}

	@Override
	public JpaVirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		JpaVirtualMachine vm = virtueDao.getVmWithApplication(virtueId, applicationId);
		return vm;
	}

	@Override
	public JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm) {
		// return vmManager.startVirtualMachine(vm);
		// assume started
		// cloudManager.startVirtualMachine(vm);
		logger.debug("**TODO**: need to implement start vm in cloud manager.  It is assumed vms are started now");
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public JpaVirtueInstance provisionTemplate(JpaVirtueUser user, JpaVirtueTemplate template) {
		try {
			JpaVirtueInstance vi = cloudManager.createVirtue(user, template);
			logger.debug("From template=" + template);
			logger.debug("  created instance=" + vi);
			virtueDao.addVirtue(vi);
			return vi;
		} catch (Exception e) {
			// TODO fix cloud manager to not throw exception. Throw something more specific.
			logger.error("error creating virtue!", e);
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "unknown error creating virtue.", e);
		}
		// List<VirtualMachineTemplate> vmTemplates = template.getVmTemplates();
		// Map<String, VirtualMachine> vms =
		// vmManager.provisionVirtualMachineTemplates(vmTemplates);
		// VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		// virtueDao.addVirtue(vi);
		// return vi;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		virtueDao.updateVmState(vmId, state);
	}

//	private class VmUpdateListener implements IStateUpdateListener {
//		@Override
//		public void updateVmState(String vmId, VmState state) {
//			updateVmState(vmId, state);
//		}
//	}

	@Override
	public void deleteVirtue(JpaVirtueUser user, String instanceId) {
		JpaVirtueInstance vi = virtueDao.getVirtueInstance(instanceId).get();
		if (vi == null) {
			throw new SaviorException(SaviorException.VIRTUE_ID_NOT_FOUND,
					"Virtue id=" + instanceId + " was not found");
		}
		if (vi.getUsername().equals(user.getUsername())) {

		} else {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User=" + user.getUsername()
					+ " does not own virtue with id=" + instanceId + " and thus cannot delete that virtue");
		}
	}

	@Override
	public void adminDeleteVirtue(String instanceId) {
		JpaVirtueInstance vi = virtueDao.getVirtueInstance(instanceId).get();
		if (vi == null) {
			throw new SaviorException(SaviorException.VIRTUE_ID_NOT_FOUND,
					"Virtue id=" + instanceId + " was not found");
		}
		cloudManager.deleteVirtue(vi);
	}

	@Override
	public Iterable<JpaVirtueInstance> getAllActiveVirtues() {
		return virtueDao.getAllActiveVirtues();
	}

	@Override
	public JpaVirtueInstance getActiveVirtue(String virtueId) {
		Optional<JpaVirtueInstance> opt = virtueDao.getVirtueInstance(virtueId);
		return opt.isPresent() ? opt.get() : null;
	}

	@Override
	public JpaVirtueInstance getVirtueForUserFromTemplateId(JpaVirtueUser user, String instanceId) {
		JpaVirtueInstance vi = virtueDao.getVirtueInstance(user, instanceId);
		return vi;
	}

	@Override
	public void updateVms(Collection<JpaVirtualMachine> vms) {
		virtueDao.updateVms(vms);

	}
}
