package com.ncc.savior.virtueadmin.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.service.VirtueUserService.StateUpdateListener;

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
	private List<StateUpdateListener> stateUpdateListeners;
	private ScheduledExecutorService executorService;
	private Random rand;

	public VirtualBoxInfrastructureService(String vmName, String hostname, int sshPort) {
		this.vmName = vmName;
		this.hostname = hostname;
		this.sshPort = sshPort;
		stateUpdateListeners = new ArrayList<StateUpdateListener>();
		executorService = new ScheduledThreadPoolExecutor(1);
		this.rand = new Random();
	}

	@Override
	public VirtueInstance provisionTemplate(User user, VirtueTemplate template, boolean useAlreadyProvisioned) {
		Map<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();
		List<VirtualMachineTemplate> templates = template.getVmTemplates();
		for (VirtualMachineTemplate vmTemplate : templates) {
			VirtualMachine vm = provisionVm(vmTemplate);
			vms.put(vm.getId(), vm);
		}
		VirtueInstance virtue = new VirtueInstance(UUID.randomUUID().toString(), template.getName(), user.getUsername(),
				template.getId(), template.getApplications(), vms, VirtueState.CREATING);
		setStatusLater(virtue, VirtueState.STOPPED);
		return virtue;
	}

	@Override
	public VirtualMachine provisionVm(VirtualMachineTemplate vmTemplate) {
		String infrastructureId = vmName;
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmTemplate.getName(),
				vmTemplate.getApplications(), VmState.CREATING, vmTemplate.getOs(), infrastructureId, this.hostname,
				this.sshPort);
		vm.setState(VmState.STOPPED);
		return vm;
	}

	@Override
	public boolean stopVirtue(VirtueInstance virtue) {
		Map<String, VirtualMachine> map = virtue.getVms();
		boolean success = true;
		for (VirtualMachine vm : map.values()) {
			success &= stopVm(vm);
		}
		return success;
	}

	public boolean stopVm(VirtualMachine vm) {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_POWEROFF,
				vm.getInfrastructureId());
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
	public boolean launchVirtue(VirtueInstance virtue) {
		Map<String, VirtualMachine> map = virtue.getVms();
		boolean success = true;
		for (VirtualMachine vm : map.values()) {
			success &= launchVm(vm);
		}
		setStatusLater(virtue, VirtueState.RUNNING);
		return success;
	}

	private void setStatusLater(VirtueInstance virtue, VirtueState state) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				executorService.schedule(new Runnable() {
					@Override
					public void run() {
						for (StateUpdateListener listener:stateUpdateListeners) {
							listener.updateVirtueState(virtue.getId(), state);
						}
					}
				}, rand.nextInt(5000) + 500, TimeUnit.MILLISECONDS);
			}
		});
	}

	public boolean launchVm(VirtualMachine vm) {
		String status = getVmStatus(vm.getInfrastructureId());
		if (status.equalsIgnoreCase("running")) {
			return true;
		}
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_START, vmName);
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
		Map<String, VirtualMachine> map = virtue.getVms();
		boolean success = true;
		for (VirtualMachine vm : map.values()) {
			success &= destroyVm(vm);
		}
		return success;
	}

	public boolean destroyVm(VirtualMachine vm) {
		// this stops instead of destroying because we currently don't create or destory
		// anything with this implementation.
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_POWEROFF,
				vm.getInfrastructureId());
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return false;
		}
	}

	public boolean pauseVm(VirtualMachine vm) {
		String id = vm.getInfrastructureId();
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_PAUSE, id);
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return false;
		}
	}

	public boolean resumeVm(VirtualMachine vm) {
		String id = vm.getInfrastructureId();
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_RESUME, id);
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

	private Map<String, String> getVms() {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_LIST);
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			HashMap<String, String> map = new HashMap<String, String>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String part = line.substring(1);
				int nameEndIndex = part.indexOf("\"");
				String name = part.substring(0, nameEndIndex);
				String id = part.substring(nameEndIndex + 3, part.length() - 1);
				id = id.trim();
				map.put(id, name);
			}
			p.waitFor();
			return map;
		} catch (IOException | InterruptedException e) {
			// TODO handle
			return null;
		}
	}

	@Override
	public void addStateUpdateListener(StateUpdateListener stateUpdateListener) {
		stateUpdateListeners.add(stateUpdateListener);
	}

	public static void main(String[] args) {
		VirtualBoxInfrastructureService vbis = new VirtualBoxInfrastructureService("", "localhost", 22);
		System.out.println(vbis.getVms());
	}
}
