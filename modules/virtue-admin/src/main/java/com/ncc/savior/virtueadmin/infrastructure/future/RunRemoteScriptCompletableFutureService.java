/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.infrastructure.future;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.future.RunRemoteScriptCompletableFutureService.ScriptGenerator;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Service which will try to run commands on a remote session. The commands come
 * from a {@link ScriptGenerator} which can be given commands or the commands
 * can be generated by a passed in function. This is often used to run commands
 * from templated scripts.
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
		try {
			session = SshUtil.getConnectedSessionWithRetries(vm, privateKeyFile, 3, 1000);
			for (String command : commands) {
				List<String> lines;
				if (timeout > 0) {
					lines = SshUtil.sendCommandFromSessionWithTimeout(session, command, timeoutMillis);
				} else {
					lines = Arrays.asList(SshUtil.runCommand(session, command, Integer.MAX_VALUE).getOutput().split("\n"));
				}
				logger.debug(lines.toString());
			}
			logger.debug("all commands run successfully");
		} finally {
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
