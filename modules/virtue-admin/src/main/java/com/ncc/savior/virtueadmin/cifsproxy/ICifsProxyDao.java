package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

public interface ICifsProxyDao {

	VirtualMachine getCifsVm(VirtueUser user);

	void updateCifsVm(VirtueUser user, VirtualMachine vm);

	void updateUserTimeout(VirtueUser user);

	long getUserTimeout(VirtueUser user);

	void deleteCifsVm(VirtueUser user);

	Set<VirtueUser> getAllUsers();

}
