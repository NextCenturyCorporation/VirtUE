package com.ncc.savior.virtueadmin.infrastructure.future;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * This service tests a VM to see if the server can reach it via SSH login. When
 * adding a VM to this service, the extra parameter is set to whether success
 * should be reachable or not reachable. This is useful to determine when a
 * Virtual Machine is finished booting or is shutting down.
 * 
 *
 */
public class TestReachabilityCompletableFuture
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Boolean> {
	private static final Logger logger = LoggerFactory.getLogger(TestReachabilityCompletableFuture.class);
	private IKeyManager keyManager;

	public TestReachabilityCompletableFuture(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, true, 3000, 3000);
		this.keyManager = keyManager;
		this.timeoutMillis = 10 * 60 * 1000;
	}

	@Override
	protected String getId(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Boolean>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	protected void onExecute(String id,
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, Boolean>.Wrapper wrapper) {
		testReachability(id, wrapper);
	}

	/**
	 * Tests the reachability of a VM and calls success if reachable is desired
	 * value.
	 * 
	 * @param id
	 * 
	 * @param wrapper
	 */
	protected void testReachability(String id,
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, Boolean>.Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		Boolean successOnReachable = wrapper.extra;
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		boolean reachable = SshUtil.isVmReachable(vm, privateKeyFile);
		if (logger.isTraceEnabled()) {
			logger.trace("Tested VM=" + vm.getId() + " reachability=" + reachable + " successOnReachable="
					+ successOnReachable);
		}
		if (reachable && successOnReachable) {
			onSuccess(id, wrapper.param, wrapper.future);
		} else if (!reachable && !successOnReachable) {
			onSuccess(id, wrapper.param, wrapper.future);
		}
	}

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return "TestReachabilityService";
	}

}
