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
 * Future service which creates a new user with a random password for windows machines.
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
			String password = createPassword();
			String user = "virtue";
			String command = String.format(
					"powershell.exe $password = ConvertTo-SecureString -AsPlainText -Force \"%s\"; New-LocalUser \"%s\" -Password $password ; Add-LocalGroupMember -Group \\\"Remote Desktop Users\\\" -Member \"%s\"",
					password, user, user);

			Session session;
			try {
				session = SshUtil.getConnectedSession(vm, keyManager.getKeyFileByName(vm.getPrivateKeyName()));
				List<String> lines = SshUtil.sendCommandFromSession(session, command);
				logger.debug("Create windows user output: " + lines);
				vm.setPassword(password);
				vm.setWindowsUser(user);
			} catch (JSchException | IOException e) {
				throw new SaviorException(SaviorErrorCode.SSH_ERROR, "Error attempting to create new windows user.", e);
			}
		}
		onSuccess(id, vm, wrapper.future);
	}

	private String createPassword() {
		int length = 12 + random.nextInt(4);
		String password = new Random().ints(length, 65, 122)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
//		password="password123";
		return password;
	}

	@Override
	protected String getServiceName() {
		return "WindowsAccountGenerator";
	}

}
