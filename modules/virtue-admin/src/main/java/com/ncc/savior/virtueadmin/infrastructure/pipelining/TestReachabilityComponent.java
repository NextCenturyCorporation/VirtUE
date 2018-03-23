package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.SshUtil;

/**
 * Component of an {@link IUpdatePipeline} that will test for reachability via
 * SSH. Success could be being reachable or unreachable depending on the
 * constructor parameter 'successOnReachable'. It will use
 * {@link VirtualMachine#getPrivateKeyName()} and a passin {@link IKeyManager}
 * to login.
 */
public class TestReachabilityComponent extends BaseIndividualVmPipelineComponent<VirtualMachine> {
	private IKeyManager keyManager;
	private boolean successOnReachable;

	public TestReachabilityComponent(ScheduledExecutorService executor, IKeyManager keyManager,
			boolean successOnReachable) {
		super(executor, true, 25000, 3000);
		this.keyManager = keyManager;
		this.successOnReachable = successOnReachable;
	}

	@Override
	protected void onExecute(PipelineWrapper<VirtualMachine> wrapper) {
		testReachability(wrapper);
	}

	/**
	 * Tests the reachability of a VM and calls success if reachable is desired
	 * value.
	 * 
	 * @param vm
	 */
	protected void testReachability(PipelineWrapper<VirtualMachine> wrapper) {
		VirtualMachine vm = wrapper.get();
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		boolean reachable = SshUtil.isVmReachable(vm, privateKeyFile);
		if (reachable && successOnReachable) {
			doOnSuccess(wrapper);
		} else if (!reachable && !successOnReachable) {
			doOnSuccess(wrapper);
		}
	}

	@Override
	protected String getId(VirtualMachine element) {
		return element.getId();
	}
}
