package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.SimpleApplicationManager;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Component of an {@link IUpdatePipeline} that will start Xpra on the
 * {@link VirtualMachine}. It will use
 * {@link VirtualMachine#getPrivateKeyName()} and a passin {@link IKeyManager}
 * to login.
 */
public class StartXpraComponent extends BaseIndividualVmPipelineComponent {
	private static final Logger logger = LoggerFactory.getLogger(StartXpraComponent.class);
	private IKeyManager keyManager;
	private SimpleApplicationManager appManager;

	public StartXpraComponent(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, false, 100, 1500);
		this.keyManager = keyManager;
		this.appManager = new SimpleApplicationManager();
	}

	@Override
	protected void onExecute(JpaVirtualMachine vm) {
		attemptStartXpra(vm);

	}

	protected void attemptStartXpra(JpaVirtualMachine vm) {
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("starting xpra get " + vm);
			}
			int display = appManager.startOrGetXpraServer(vm, privateKeyFile);
			if (display > 0) {
				vm.setState(VmState.RUNNING);
				doOnSuccess(vm);
			}
		} catch (IOException e) {
			logger.debug("Failed to start XPRA", e);
		}
	}
}
