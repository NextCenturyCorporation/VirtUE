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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.service.IStateUpdateListener;

public class VirtualBoxInfrastructureService implements IInfrastructureService {
	private static final Logger logger = LoggerFactory.getLogger(VirtualBoxInfrastructureService.class);

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
	private List<IStateUpdateListener> stateUpdateListeners;
	private ScheduledExecutorService executorService;
	private Random rand;

	public VirtualBoxInfrastructureService(String vmName, String hostname, int sshPort) {
		this.vmName = vmName;
		this.hostname = hostname;
		this.sshPort = sshPort;
		stateUpdateListeners = new ArrayList<IStateUpdateListener>();
		executorService = new ScheduledThreadPoolExecutor(1);
		this.rand = new Random();
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

	private void setStatusLater(VirtueInstance virtue, VirtueState state) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				executorService.schedule(new Runnable() {
					@Override
					public void run() {
						for (IStateUpdateListener listener : stateUpdateListeners) {
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
		logger.debug(getVms().toString());
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_GETINFO, vmName);
		try {
			Process p = Runtime.getRuntime().exec(command);
			Properties props = new Properties();
			props.load(p.getInputStream());
			p.waitFor();
			String state = (String) props.get("VMState");
			state = state.replaceAll("\"", "");
			return state;
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
	public void addStateUpdateListener(IStateUpdateListener stateUpdateListener) {
		stateUpdateListeners.add(stateUpdateListener);
	}

	@Override
	public VirtueInstance getProvisionedVirtueFromTemplate(User user, VirtueTemplate template) {
		Map<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();
		VmState state = statusToState(getVmStatus(vmName));
		OS os = OS.LINUX;
		String infrastructureId = vmName;
		for (VirtualMachineTemplate vmt : template.getVmTemplates()) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
					state, os, infrastructureId, hostname, sshPort);
			vms.put(vm.getId(), vm);
		}
		Map<String, ApplicationDefinition> apps = template.getApplications();
		String username = user.getUsername();
		return new VirtueInstance(UUID.randomUUID().toString(), template.getName(), username, template.getId(), apps,
				vms);
	}

	private VmState statusToState(String vmStatus) {
		switch (vmStatus) {
		case "running":
			return VmState.RUNNING;
		default:
			logger.warn("Need to define status ot state for virtualbox. vmStatus=" + vmStatus);
			return null;
		}
	}

	@Override
	public VirtualMachine startVm(VirtualMachine vm) {
		launchVm(vm);
		String status = getVmStatus(vm.getInfrastructureId());
		VmState state = statusToState(status);
		vm.setState(state);
		return vm;
	}
}
