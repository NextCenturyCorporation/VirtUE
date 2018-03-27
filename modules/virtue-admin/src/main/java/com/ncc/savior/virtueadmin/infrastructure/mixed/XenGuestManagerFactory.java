package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.File;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

public class XenGuestManagerFactory {

	private IKeyManager keyManager;
	private IUpdateListener<VirtualMachine> notifier;

	public XenGuestManagerFactory(IKeyManager keyManager, IUpdateListener<VirtualMachine> notifier) {
		this.keyManager = keyManager;
		this.notifier = notifier;
	}

	public XenGuestManager getXenGuestManager(VirtualMachine xenVm) {
		String keyName = xenVm.getPrivateKeyName();
		File keyFile = keyManager.getKeyFileByName(keyName);
		XenGuestManager xgm = new XenGuestManager(xenVm, keyFile, notifier);
		return xgm;
	}

}
