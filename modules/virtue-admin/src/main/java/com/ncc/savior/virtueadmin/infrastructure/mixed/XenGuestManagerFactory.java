package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.File;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Factory class to get {@link XenGuestManager}s for specific Xen host machines.
 * 
 *
 */
public class XenGuestManagerFactory {

	private IKeyManager keyManager;
	private Route53Manager route53;
	private CompletableFutureServiceProvider serviceProvider;

	public XenGuestManagerFactory(IKeyManager keyManager, CompletableFutureServiceProvider serviceProvider,
			Route53Manager route53) {
		this.keyManager = keyManager;
		this.route53 = route53;
		this.serviceProvider = serviceProvider;
	}

	public XenGuestManager getXenGuestManager(VirtualMachine xenVm) {
		String keyName = xenVm.getPrivateKeyName();
		File keyFile = keyManager.getKeyFileByName(keyName);
		XenGuestManager xgm = new XenGuestManager(xenVm, keyFile, serviceProvider, route53);
		return xgm;
	}

}
