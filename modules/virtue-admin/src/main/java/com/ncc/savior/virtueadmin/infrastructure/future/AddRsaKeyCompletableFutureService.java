package com.ncc.savior.virtueadmin.infrastructure.future;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;

public class AddRsaKeyCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory.getLogger(AddRsaKeyCompletableFutureService.class);
	private IKeyManager keyManager;
	private SshKeyInjector sshKeyInjector;

	public AddRsaKeyCompletableFutureService(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, true, 25000, 3000);
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
	}

	@Override
	protected String getId(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	protected void onExecute(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		// logger.debug("Testing if VM is reachable - " + vm);
		try {
			logger.debug("VM is reachable - " + vm);
			String newPrivateKey = null;
			try {
				newPrivateKey = sshKeyInjector.injectSshKey(vm, privateKeyFile);
				vm.setPrivateKey(newPrivateKey);
				onSuccess(vm.getId(), vm, wrapper.future);
			} catch (Exception e) {
				logger.error("Injecting new SSH key failed.  Retrying.", e);
			} finally {

			}
		} catch (Throwable t) {
			logger.error("Test Failed - " + vm, t);
		}

	}

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return "AddRsaService";
	}
}
