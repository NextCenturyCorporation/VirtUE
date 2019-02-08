package com.ncc.savior.virtueadmin.infrastructure.windows;

import java.util.Collection;
import java.util.List;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Dao than handles storing existence of Windows Display Server VMs.
 *
 */
public interface IWindowsDisplayServerDao {

	VirtualMachine getDisplayServerVmByWindowsApplicationVmId(String windowsApplicationVmId);

	void updateDisplayServerVm(String username, String windowsApplicationVmId, VirtualMachine vm);

	void deleteDisplayServerVmId(String windowsApplicationVmId);

	void deleteDisplayServerVmsForUser(String username);

	Collection<VirtualMachine> getDisplayServerVmsByWindowsApplicationVmIds(List<String> windowsApplicationVmIds);
}
