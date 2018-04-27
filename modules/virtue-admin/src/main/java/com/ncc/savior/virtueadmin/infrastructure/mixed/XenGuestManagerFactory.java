package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.File;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Factory class to get {@link XenGuestManager}s for specific Xen host machines.
 * 
 *
 */
public class XenGuestManagerFactory {

	private IKeyManager keyManager;
	private IUpdateListener<VirtualMachine> notifier;
	private XenGuestVmUpdater guestVmUpdater;
	private Route53Manager route53;

	public XenGuestManagerFactory(IKeyManager keyManager, IUpdateListener<VirtualMachine> notifier,
			Route53Manager route53) {
		this.keyManager = keyManager;
		this.notifier = notifier;
		this.route53 = route53;
		guestVmUpdater = new XenGuestVmUpdater(notifier, keyManager);
	}

	public XenGuestManager getXenGuestManager(VirtualMachine xenVm) {
		String keyName = xenVm.getPrivateKeyName();
		File keyFile = keyManager.getKeyFileByName(keyName);
		XenGuestManager xgm = new XenGuestManager(xenVm, keyFile, notifier, guestVmUpdater, route53);
		return xgm;
	}

}
