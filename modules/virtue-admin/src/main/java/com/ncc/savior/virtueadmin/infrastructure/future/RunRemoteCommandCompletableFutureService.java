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
import com.ncc.savior.virtueadmin.infrastructure.future.RunRemoteCommandCompletableFutureService.CommandGenerator;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Service which will try to run a remote command or fail.
 *
 */
public class RunRemoteCommandCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, CommandGenerator> {
	private static final Logger logger = LoggerFactory.getLogger(RunRemoteCommandCompletableFutureService.class);
	private IKeyManager keyManager;

	public RunRemoteCommandCompletableFutureService(ScheduledExecutorService executor, IKeyManager keyManager) {
		super(executor, true, 10, 1000, 30000);
		this.keyManager = keyManager;
	}

	@Override
	protected String getId(
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, CommandGenerator>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	protected void onExecute(String id,
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, CommandGenerator>.Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		// logger.debug("Testing if VM is reachable - " + vm);
		String command = null;
		try {
			command = wrapper.extra.getCommand(vm);
			int timeout = wrapper.extra.getTimeoutMillis();
			runCommand(vm, command, timeout);
			onSuccess(id, vm, wrapper.future);
		} catch (Throwable t) {
			logger.error("Command failed.  Command=" + command + "vm=" + vm, t);
			onFailure(id, vm, wrapper.future);
		}

	}

	private void runCommand(VirtualMachine vm, String command, int timeout) throws IOException, JSchException {
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());

		// Jsch is not thread safe
		Session session = null;
		ChannelExec channel = null;
		try {
			session = SshUtil.getConnectedSessionWithRetries(vm, privateKeyFile, 3, 1000);
			List<String> lines;
			if (timeout > 0) {
				lines = SshUtil.sendCommandFromSessionWithTimeout(session, command, timeoutMillis);
			} else {
				lines = SshUtil.sendCommandFromSession(session, command);
			}
			logger.debug(lines.toString());
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

	public static class CommandGenerator {
		private String command;
		private int timeoutMillis;
		private Function<VirtualMachine, String> function;

		public CommandGenerator(String command) {
			this.command = command;
		}

		public int getTimeoutMillis() {
			return this.timeoutMillis;
		}

		public void setTimeoutMillis(int timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
		}

		public CommandGenerator(Function<VirtualMachine, String> function) {
			this.function = function;
		}

		public String getCommand(VirtualMachine vm) {
			if (command == null) {
				command = function.apply(vm);
			}
			return command;
		}
	}
}
