/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.virtue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.cifsproxy.CifsManager;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Implementation of {@link IActiveVirtueManager}.
 *
 * See interface for more descriptions.
 *
 */
public class ActiveVirtueManager implements IActiveVirtueManager, IUpdateListener<VirtualMachine> {
	private final static Logger logger = LoggerFactory.getLogger(ActiveVirtueManager.class);

	private IActiveVirtueDao virtueDao;
	private ICloudManager cloudManager;

	private Set<VirtueCreationDeletionListener> virtueCreationDeletionListeners;

	public ActiveVirtueManager(ICloudManager cloudManager, IActiveVirtueDao virtueDao, CifsManager cifsManager) {
		this.cloudManager = cloudManager;
		this.virtueDao = virtueDao;
		this.virtueCreationDeletionListeners = new HashSet<VirtueCreationDeletionListener>();
		addVirtueCreationDeletionListener(cifsManager.getVirtueCreationDeletionListener());
		cifsManager.setActiveVirtueManager(this);
		cloudManager.setCifsManager(cifsManager);
	}

	@Override
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> templateIds) {
		Map<String, Set<VirtueInstance>> virtues = virtueDao.getVirtuesFromTemplateIds(user, templateIds);
		return virtues;
	}

	@Override
	public Collection<VirtueInstance> getVirtuesForUser(VirtueUser user) {
		return virtueDao.getVirtuesForUser(user);
	}

	@Override
	public VirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		VirtualMachine vm = virtueDao.getVmWithApplication(virtueId, applicationId);
		return vm;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		// return vmManager.startVirtualMachine(vm);
		// assume started
		// cloudManager.startVirtualMachine(vm);
		logger.debug("**TODO**: need to implement start vm in cloud manager.  It is assumed vms are started now");
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public VirtueInstance provisionTemplate(VirtueUser user, VirtueTemplate template) {
		try {
			VirtueInstance vi = cloudManager.createVirtue(user, template);
			logger.debug("From template=" + template + " created instance=" + vi);
			virtueDao.addVirtue(vi);
			onVirtueCreation(vi, template);
			return vi;
		} catch (Exception e) {
			// TODO fix cloud manager to not throw exception. Throw something more specific.
			logger.error("error creating virtue!", e);
			if (e instanceof SaviorException) {
				throw (SaviorException) e;
			} else {
				throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, "unknown error creating virtue.", e);
			}
		}
		// List<VirtualMachineTemplate> vmTemplates = template.getVmTemplates();
		// Map<String, VirtualMachine> vms =
		// vmManager.provisionVirtualMachineTemplates(vmTemplates);
		// VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		// virtueDao.addVirtue(vi);
		// return vi;
	}

	// private class VmUpdateListener implements IStateUpdateListener {
	// @Override
	// public void updateVmState(String vmId, VmState state) {
	// updateVmState(vmId, state);
	// }
	// }

	@Override
	public VirtueInstance deleteVirtue(VirtueUser user, String instanceId) {
		VirtueInstance vi = virtueDao.getVirtueInstance(instanceId).get();
		if (vi == null) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Virtue id=" + instanceId + " was not found");
		}

		if (vi.getUsername().equals(user.getUsername()) || VirtueUser.isAdmin(user)) {
			CompletableFuture<VirtueInstance> future = new CompletableFuture<VirtueInstance>();
			cloudManager.deleteVirtue(vi, future);
			future.thenAccept((virtue) -> {
				virtueDao.deleteVirtue(virtue);
				onVirtueDeletion(virtue);
			});
		} else {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED,
					"User=" + user.getUsername() + " does not own virtue with id=" + instanceId
							+ " and is not admin.  Therefore, " + user.getUsername() + " cannot delete that virtue");
		}

		return vi;
	}

	@Override
	public void adminDeleteVirtue(String instanceId) {
		Optional<VirtueInstance> opt = virtueDao.getVirtueInstance(instanceId);
		if (!opt.isPresent()) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Virtue id=" + instanceId + " was not found");
		} else {
			VirtueInstance vi = opt.get();
			CompletableFuture<VirtueInstance> future = new CompletableFuture<VirtueInstance>();
			cloudManager.deleteVirtue(vi, future);
			future.thenAccept((virtue) -> {
				virtueDao.deleteVirtue(virtue);
				onVirtueDeletion(virtue);
			});
		}
	}

	@Override
	public Iterable<VirtueInstance> getAllActiveVirtues() {
		return virtueDao.getAllActiveVirtues();
	}

	@Override
	public VirtueInstance getActiveVirtue(String virtueId) {
		Optional<VirtueInstance> opt = virtueDao.getVirtueInstance(virtueId);
		if (opt.isPresent()) {
			return opt.get();
		} else {
			// We throw an exception here mainly because nulls are handled strangely by
			// Jersey (They return a 204 which means success with no content). Throwing an
			// exception allows our exception handling system deal with the error better.
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Cannot find virtue with id=" + virtueId);
		}
	}

	@Override
	public VirtueInstance getVirtueForUserFromTemplateId(VirtueUser user, String instanceId) {
		VirtueInstance vi = virtueDao.getVirtueInstance(user, instanceId);
		if (vi == null) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND,
					"Cannot find virtue with id=" + instanceId + " for user=" + user.getUsername());
		}
		return vi;
	}

	@Override
	public VirtueInstance startVirtue(VirtueUser user, String virtueId) {
		Optional<VirtueInstance> v = virtueDao.getVirtueInstance(virtueId);
		if (v.isPresent()) {
			VirtueInstance virtue = v.get();
			if (virtue.getUsername().equals(user.getUsername())) {
				virtue = cloudManager.startVirtue(virtue);
				return virtue;
			} else {
				throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED,
						"User=" + user.getUsername() + " is not authorized to start virtueId=" + virtueId);
			}
		} else {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Could not find virtue with ID=" + virtueId);
		}
	}

	@Override
	public VirtueInstance stopVirtue(VirtueUser user, String virtueId) {
		Optional<VirtueInstance> v = virtueDao.getVirtueInstance(virtueId);
		if (v.isPresent()) {
			VirtueInstance virtue = v.get();
			if (virtue.getUsername().equals(user.getUsername()) || VirtueUser.isAdmin(user)) {
				virtue = cloudManager.stopVirtue(virtue);
				return virtue;
			} else {
				throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED,
						"User=" + user.getUsername() + " is not authorized to stop virtueId=" + virtueId);
			}
		} else {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Could not find virtue with ID=" + virtueId);
		}
	}

	@Override
	public void updateElements(Collection<VirtualMachine> vms) {
		virtueDao.updateVms(vms);

	}

	@Override
	public VirtualMachine getVm(String id) {
		Optional<VirtualMachine> vm = virtueDao.getXenVm(id);
		if (vm.isPresent()) {
			return vm.get();
		} else {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Could not find vm with ID=" + id);
		}
	}

	@Override
	public Iterable<VirtualMachine> getAllVirtualMachines() {
		return virtueDao.getAllVirtualMachines();
	}

	@Override
	public void rebootVm(String vmId) {
		Optional<VirtualMachine> vm = virtueDao.getXenVm(vmId);
		VirtualMachine vmToReboot;

		if (vm.isPresent()) {
			vmToReboot = vm.get();
		} else {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Could not find vm with ID=" + vmId);
		}

		VirtueInstance virtue = virtueDao.getVirtueByVmId(vmId);

		if (virtue != null) {
			cloudManager.rebootVm(vmToReboot, virtue.getId());
		} else {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Could not find virtue with the vm ID=" + vmId);
		}
	}

	@Override
	public void sync() {
		Iterable<VirtueInstance> virtues = virtueDao.getAllActiveVirtues();
		List<String> ids = new ArrayList<String>();
		for (VirtueInstance v : virtues) {
			ids.add(v.getId());
		}
		cloudManager.sync(ids);
	}

	// private class VmUpdateListener implements IStateUpdateListener {
	// @Override
	// public void updateVmState(String vmId, VmState state) {
	// updateVmState(vmId, state);
	// }
	// }
	@Override
	public void addVirtueCreationDeletionListener(VirtueCreationDeletionListener vcdl) {
		virtueCreationDeletionListeners.add(vcdl);
	}

	private void onVirtueCreation(VirtueInstance virtue, VirtueTemplate template) {
		for (VirtueCreationDeletionListener listener : virtueCreationDeletionListeners) {
			try {
				listener.onVirtueCreation(virtue, template);
			} catch (Exception e) {
				logger.warn("Failed to notify virtue creation! Virtue=" + virtue, e);
			}
		}
	}

	private void onVirtueDeletion(VirtueInstance virtue) {
		for (VirtueCreationDeletionListener listener : virtueCreationDeletionListeners) {
			try {
				listener.onVirtueDeletion(virtue);
			} catch (Exception e) {
				logger.warn("Failed to notify virtue creation! Virtue=" + virtue, e);
			}
		}
	}

	public static interface VirtueCreationDeletionListener {
		void onVirtueCreation(VirtueInstance virtue, VirtueTemplate template);

		void onVirtueDeletion(VirtueInstance virtue);
	}
}
