package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Map;

import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.util.SaviorException;

import persistance.JpaVirtualMachine;
import persistance.JpaVirtualMachineTemplate;

/**
 * {@link ICloudManager} implementation which delegates individual VM actions to
 * multiple {@link IVmManager}s based on the OS of the VM.
 *
 */
public class OsBasedDelegatedCloudManager extends BaseDelegatedCloudManager {

	private Map<OS, IVmManager> managerMap;

	public OsBasedDelegatedCloudManager(Map<OS, IVmManager> managerMap) {
		this.managerMap = managerMap;
	}

	@Override
	protected IVmManager getVmManagerForVm(JpaVirtualMachine vm) {
		IVmManager manager = managerMap.get(vm.getOs());
		if (manager == null) {
			throw new SaviorException(SaviorException.CONFIGURATION_ERROR,
					this.getClass().getCanonicalName() + " not configured with IVmManager for OS=" + vm.getOs());
		}
		return manager;
	}

	@Override
	protected IVmManager getVmManagerForVmTemplate(JpaVirtualMachineTemplate vmt) {
		IVmManager manager = managerMap.get(vmt.getOs());
		if (manager == null) {
			throw new SaviorException(SaviorException.CONFIGURATION_ERROR,
					this.getClass().getCanonicalName() + " not configured with IVmManager for OS=" + vmt.getOs());
		}
		return manager;
	}
}
