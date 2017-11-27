package com.ncc.savior.virtueadmin.infrastructure;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public class VirtualBoxInfrastructureService implements IInfrastructureService {

	private static final String WINDOWS_PATH_TO_VIRTUAL_BOX = "C:\\Program Files\\Oracle\\VirtualBox\\";
	private static final String COMMAND = "VBoxManage";
	private static final String ARGS_LIST = "list vms";
	private static final String ARGS_START = "startvm \"%s\" --type headless";
	private static final String ARGS_PAUSE = "controlvm \"%s\" pause --type headless";
	private static final String ARGS_RESUME = "controlvm \"%s\" resume --type headless";
	private static final String ARGS_POWEROFF = "controlvm \"%s\" poweroff --type headless";
	private static final String ARGS_GETINFO = "showvminfo \"%s\" --machinereadable";

	private String hostname;
	private int sshPort;
	private String vmName;

	public VirtualBoxInfrastructureService(String vmName, String hostname, int sshPort) {
		this.vmName = vmName;
		this.hostname = hostname;
		this.sshPort = sshPort;
	}

	@Override
	public VirtueInstance provisionTemplate(User user, VirtueTemplate template, boolean useAlreadyProvisioned) {
		VirtueInstance virtue = new VirtueInstance(UUID.randomUUID().toString(), user.getUsername(), template.getId(),
				template.getApplicationIds(), template.getStartingTransducerIds(), hostname);
		return virtue;
	}

	@Override
	public boolean launchVirtue(VirtueInstance virtue) {
		String status = getVmStatus(vmName);
		if (status.equalsIgnoreCase("running")) {
			return true;
		}
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX+COMMAND+" "+ARGS_START, vmName);
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return false;
		}
	}

	@Override
	public boolean destroyVirtue(VirtueInstance virtue) {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_POWEROFF, vmName);
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return false;
		}
	}

	@Override
	public boolean stopVirtue(VirtueInstance virtue) {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_POWEROFF, vmName);
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return false;
		}
	}

	private String getVmStatus(String uuidOrName) {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_GETINFO, vmName);
		try {
			Process p = Runtime.getRuntime().exec(command);
			Properties props = new Properties();
			props.load(p.getInputStream());
			p.waitFor();
			return (String) props.get("VMState");
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return null;
		}
	}

}
