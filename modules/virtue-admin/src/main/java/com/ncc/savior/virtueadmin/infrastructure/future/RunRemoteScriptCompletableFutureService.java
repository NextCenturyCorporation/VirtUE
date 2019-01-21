package com.ncc.savior.virtueadmin.infrastructure.future;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.future.RunRemoteScriptCompletableFutureService.ScriptGenerator;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Service which will try to run a remote command or fail.
 * 
 *
 */
public class RunRemoteScriptCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, ScriptGenerator> {
	private static final Logger logger = LoggerFactory.getLogger(RunRemoteScriptCompletableFutureService.class);
	private IKeyManager keyManager;

	public RunRemoteScriptCompletableFutureService(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, true, 10, 1000, 30000);
		this.keyManager = keyManager;
	}

	@Override
	protected String getId(
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, ScriptGenerator>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	protected void onExecute(String id,
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, ScriptGenerator>.Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		String[] commands = null;
		try {
			commands = wrapper.extra.getCommands(vm);
			int timeout = wrapper.extra.getTimeoutMillis();
			if (wrapper.extra.isDryRun()) {
				logCommands(vm, commands);
			} else {
				runCommands(vm, commands, timeout);
			}
			onSuccess(id, vm, wrapper.future);
		} catch (Throwable t) {
			logger.error(
					"Command failed.  Commands=" + (commands == null ? "[]" : String.join(",", commands)) + "vm=" + vm,
					t);
			onFailure(id, vm, wrapper.future);
		}

	}

	private void logCommands(VirtualMachine vm, String[] commands) {
		String output = String.join(System.lineSeparator(), commands);
		logger.debug("Dryrun to " + vm.getHostname() + System.lineSeparator() + output);

	}

	private void runCommands(VirtualMachine vm, String[] commands, int timeout) throws IOException, JSchException {
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());

		// Jsch is not thread safe
		Session session = null;
		ChannelExec channel = null;
		try {
			session = SshUtil.getConnectedSessionWithRetries(vm, privateKeyFile, 3, 1000);
			List<String> lines;
//			List<String> totalOutput=new ArrayList<String>();
			for (String command : commands) {
				if (timeout > 0) {
					lines = SshUtil.sendCommandFromSessionWithTimeout(session, command, timeoutMillis);
				} else {
					lines = SshUtil.sendCommandFromSession(session, command);
				}
				logger.debug(lines.toString());
			}

		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}

	}

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return "RunRemoteCommand";
	}

	public static class ScriptGenerator {
		private String[] commands;
		private int timeoutMillis;
		private Function<VirtualMachine, String[]> function;
		private boolean dryRun;

		public ScriptGenerator(String[] commands) {
			this.commands = commands;
		}

		public int getTimeoutMillis() {
			return this.timeoutMillis;
		}

		public void setTimeoutMillis(int timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
		}

		public ScriptGenerator(Function<VirtualMachine, String[]> function) {
			this.function = function;
		}

		public String[] getCommands(VirtualMachine vm) {
			if (commands == null) {
				commands = function.apply(vm);
			}
			return commands;
		}

		public boolean isDryRun() {
			return this.dryRun;
		}

		public void setDryRun(boolean b) {
			this.dryRun = true;
		}
	}
}