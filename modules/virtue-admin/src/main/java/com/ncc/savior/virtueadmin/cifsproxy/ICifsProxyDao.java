package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Dao than handles storing existance and timeout for CIFS Proxy VMs as well as
 * their timeout time.
 *
 */
public interface ICifsProxyDao {

	VirtualMachine getCifsVm(VirtueUser user);

	void updateCifsVm(VirtueUser user, VirtualMachine vm);

	void updateUserTimeout(VirtueUser user);

	long getUserTimeout(VirtueUser user);

	void deleteCifsVm(VirtueUser user);

	Set<VirtueUser> getAllUsers();

}
