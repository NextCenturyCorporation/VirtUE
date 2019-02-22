package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.File;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.template.ITemplateService;

/**
 * Factory class to get {@link XenGuestManager}s for specific Xen host machines.
 * 
 *
 */
public class XenGuestManagerFactory {

	private IKeyManager keyManager;
	private Route53Manager route53;
	private CompletableFutureServiceProvider serviceProvider;
	private ITemplateService templateService;

	public XenGuestManagerFactory(IKeyManager keyManager, CompletableFutureServiceProvider serviceProvider,
			Route53Manager route53, ITemplateService templateService) {
		this.keyManager = keyManager;
		this.route53 = route53;
		this.serviceProvider = serviceProvider;
		this.templateService = templateService;
	}

	public XenGuestManager getXenGuestManager(VirtualMachine xenVm) {
		String keyName = xenVm.getPrivateKeyName();
		File keyFile = keyManager.getKeyFileByName(keyName);
		XenGuestManager xgm = new XenGuestManager(xenVm, keyFile, serviceProvider, route53,templateService);
		return xgm;
	}

}
