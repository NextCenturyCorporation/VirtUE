package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

/**
 * Component of an {@link IUpdatePipeline} that will test for reachability via
 * SSH and once the {@link VirtualMachine} is reachable, it will add a new
 * unique RSA key via {@link SshKeyInjector}. It will use
 * {@link VirtualMachine#getPrivateKeyName()} and a passin {@link IKeyManager}
 * to login.
 */
public class TestReachabilityAndAddRsaComponent extends BaseIndividualVmPipelineComponent<VirtualMachine> {
	private static final Logger logger = LoggerFactory.getLogger(TestReachabilityAndAddRsaComponent.class);
	private IKeyManager keyManager;
	private SshKeyInjector sshKeyInjector;
	private VmState successState;
	private boolean updateVmPrivateKey;

	public TestReachabilityAndAddRsaComponent(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, true, 25000, 3000);
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
		this.successState = VmState.RUNNING;
		this.updateVmPrivateKey = true;
	}

	public TestReachabilityAndAddRsaComponent(ScheduledExecutorService executor, IKeyManager keyManager,
			long initialDelayMillis) {
		super(executor, true, initialDelayMillis, 3000);
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
		this.successState = VmState.RUNNING;
		this.updateVmPrivateKey = true;
	}

	public TestReachabilityAndAddRsaComponent(ScheduledExecutorService executor, IKeyManager keyManager,
			boolean updateVmPrivateKey) {
		super(executor, true, 25000, 3000);
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
		this.successState = VmState.RUNNING;
		this.updateVmPrivateKey = updateVmPrivateKey;
	}

	@Override
	protected void onExecute(PipelineWrapper<VirtualMachine> wrapper) {
		testReachabilityAndAddRsaKey(wrapper);
	}

	/**
	 * Tests the reachability of a VM. If reachable, add a new unique RSA key and
	 * add it to the VM and notify via the notifier.
	 * 
	 * @param vm
	 */
	protected void testReachabilityAndAddRsaKey(PipelineWrapper<VirtualMachine> wrapper) {
		VirtualMachine vm = wrapper.get();
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		// logger.debug("Testing if VM is reachable - " + vm);
		try {
			if (SshUtil.isVmReachable(vm, privateKeyFile)) {
				logger.debug("VM is reachable - " + vm);
				String newPrivateKey = null;
				try {
					newPrivateKey = sshKeyInjector.injectSshKey(vm, privateKeyFile);
					if (updateVmPrivateKey) {
						vm.setPrivateKey(newPrivateKey);
					}
					vm.setState(successState);
					doOnSuccess(wrapper);
				} catch (Exception e) {
					logger.error("Injecting new SSH key failed.  Clients will not be able to login.", e);
				} finally {

				}
			} else {
				// logger.debug("Test failed - " + vm);
			}
		} catch (Throwable t) {
			logger.error("Test Failed - " + vm, t);
		}
	}

	@Override
	protected String getId(VirtualMachine element) {
		return element.getId();
	}

	public void setSuccessState(VmState state) {
		this.successState = state;
	}
}
