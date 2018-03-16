package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

import net.bytebuddy.agent.VirtualMachine;
import persistance.JpaVirtualMachine;

/**
 * Component of an {@link IUpdatePipeline} that will test for reachability via
 * SSH and once the {@link VirtualMachine} is reachable, it will add a new
 * unique RSA key via {@link SshKeyInjector}. It will use
 * {@link VirtualMachine#getPrivateKeyName()} and a passin {@link IKeyManager}
 * to login.
 */
public class TestReachabilityAndAddRsaComponent extends BaseIndividualVmPipelineComponent {
	private static final Logger logger = LoggerFactory.getLogger(TestReachabilityAndAddRsaComponent.class);
	private IKeyManager keyManager;
	private SshKeyInjector sshKeyInjector;

	public TestReachabilityAndAddRsaComponent(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, true, 25000, 3000);
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
	}

	@Override
	protected void onExecute(JpaVirtualMachine vm) {
		testReachabilityAndAddRsaKey(vm);
	}

	/**
	 * Tests the reachability of a VM. If reachable, add a new unique RSA key and
	 * add it to the VM and notify via the notifier.
	 * 
	 * @param vm
	 */
	protected void testReachabilityAndAddRsaKey(JpaVirtualMachine vm) {
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		logger.trace("Testing if VM is reachable - " + vm);
		if (SshUtil.isVmReachable(vm, privateKeyFile)) {
			logger.trace("VM is reachable - " + vm);
			String newPrivateKey = null;
			try {
				newPrivateKey = sshKeyInjector.injectSshKey(vm, privateKeyFile);
				vm.setPrivateKey(newPrivateKey);
				doOnSuccess(vm);
			} catch (Exception e) {
				logger.error("Injecting new SSH key failed.  Clients will not be able to login.", e);
			} finally {

			}
		}
	}
}
