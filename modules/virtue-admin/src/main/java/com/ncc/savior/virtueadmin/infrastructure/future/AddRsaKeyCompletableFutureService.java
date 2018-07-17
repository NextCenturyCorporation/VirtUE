package com.ncc.savior.virtueadmin.infrastructure.future;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;

/**
 * Service which will try to add a new, unique RSA key to the SSH authorized
 * keys file on the given machine. This service will retry until successful and
 * then complete the future.
 * 
 *
 */
public class AddRsaKeyCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory.getLogger(AddRsaKeyCompletableFutureService.class);
	private IKeyManager keyManager;
	private SshKeyInjector sshKeyInjector;

	public AddRsaKeyCompletableFutureService(ScheduledExecutorService executor, IKeyManager keyManager,
			int timeoutMillis) {
		super(executor, true, 10, 1000, timeoutMillis);
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
	}

	@Override
	protected String getId(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	protected void onExecute(String id,
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		// logger.debug("Testing if VM is reachable - " + vm);
		try {
			logger.debug("VM is reachable - " + vm);
			String newPrivateKey = null;
			try {
				newPrivateKey = sshKeyInjector.injectSshKey(vm, privateKeyFile);
				vm.setPrivateKey(newPrivateKey);
				onSuccess(id, vm, wrapper.future);
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
