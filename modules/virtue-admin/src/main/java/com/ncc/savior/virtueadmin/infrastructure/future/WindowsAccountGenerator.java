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

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Future service which creates a new user with a random password for windows
 * machines.
 * 
 *
 */
public class WindowsAccountGenerator
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory.getLogger(WindowsAccountGenerator.class);

	private Random random;
	private IKeyManager keyManager;

	public WindowsAccountGenerator(ScheduledExecutorService executor, boolean isFixedRate, long initialDelayMillis,
			long periodOrDelayMillis, int timeoutMillis, IKeyManager keyManager) {
		super(executor, isFixedRate, initialDelayMillis, periodOrDelayMillis, timeoutMillis);
		this.keyManager = keyManager;
		random = new Random();
	}

	@Override
	protected String getId(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	protected void onExecute(String id,
			BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		if (OS.WINDOWS.equals(vm.getOs())) {
			Session session = null;
			try {
				session = SshUtil.getConnectedSession(vm, keyManager.getKeyFileByName(vm.getPrivateKeyName()));
			} catch (JSchException e) {
				logger.debug("could not connect to Windows VM " + vm.getUserName() + "@" + vm.getHostname() + ":"
						+ vm.getSshPort() + ": " + e);
				throw new SaviorException(SaviorErrorCode.SSH_ERROR, "Error connecting to Windows VM.", e);
			}
			
			String password = createPassword();
			String user = "virtue";
			String command = String.format(
					"powershell.exe $password = ConvertTo-SecureString -AsPlainText -Force \"%s\"; New-LocalUser \"%s\" -Password $password ; Add-LocalGroupMember -Group \\\"Remote Desktop Users\\\" -Member \"%s\"",
					password, user, user);
			try {
				List<String> lines = SshUtil.sendCommandFromSession(session, command);
				logger.debug("Create windows user output: " + lines);
			} catch (JSchException | IOException e) {
				logger.debug("error creating Windows user with command: " + command);
				throw new SaviorException(SaviorErrorCode.SSH_ERROR, "Error attempting to create new Windows user.", e);
			}
			vm.setPassword(password);
			vm.setWindowsUser(user);
		}
		onSuccess(id, vm, wrapper.future);
	}

	private String createPassword() {
		int length = 22 + random.nextInt(4);
		String password = new Random().ints(length, 65, 90)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		// password="password123";
		return password;
	}

	@Override
	protected String getServiceName() {
		return "WindowsAccountGenerator";
	}

}
