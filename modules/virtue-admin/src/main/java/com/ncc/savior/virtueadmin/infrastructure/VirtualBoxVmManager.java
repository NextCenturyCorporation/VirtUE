package com.ncc.savior.virtueadmin.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;
import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * Virtual Box based implementation of {@link IVmManager}.
 * 
 * See interface for function comments.
 * 
 *
 */
public class VirtualBoxVmManager extends BaseVmManager implements IVmManager {

	private static final Logger logger = LoggerFactory.getLogger(VirtualBoxVmManager.class);
	private static final String WINDOWS_PATH_TO_VIRTUAL_BOX = "C:\\Program Files\\Oracle\\VirtualBox\\";
	private static final String COMMAND = "VBoxManage";
	private static final String ARGS_LIST = "list vms";
	private static final String ARGS_START = "startvm \"%s\" --type headless";
	private static final String ARGS_PAUSE = "controlvm \"%s\" pause --type headless";
	private static final String ARGS_RESUME = "controlvm \"%s\" resume --type headless";
	private static final String ARGS_POWEROFF = "controlvm \"%s\" poweroff --type headless";
	private static final String ARGS_GETINFO = "showvminfo \"%s\" --machinereadable";

	@Override
	public JpaVirtualMachine provisionVirtualMachineTemplate(JpaVirtueUser user, JpaVirtualMachineTemplate vmt) {
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm) {
		VmState state = getVirtualMachineState(vm);
		if (state.equals(VmState.RUNNING)) {
			vm.setState(VmState.RUNNING);
			return vm;
		}
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_START,
				vm.getInfrastructureId());
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			vm.setState(VmState.RUNNING);
			return vm;
		} catch (IOException | InterruptedException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error attempting to start VM.  VM=" + vm, e);
		}
	}

	@Override
	public JpaVirtualMachine stopVirtualMachine(JpaVirtualMachine vm) {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_POWEROFF,
				vm.getInfrastructureId());
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			vm.setState(VmState.STOPPED);
			return vm;
		} catch (IOException | InterruptedException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error attempting to stop VM.  VM=" + vm, e);
		}
	}

	@Override
	public void deleteVirtualMachine(JpaVirtualMachine vm) {
		// this stops instead of destroying because we currently don't create or destory
		// anything with this implementation.
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_POWEROFF,
				vm.getInfrastructureId());
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error attempting to delete VM.  VM=" + vm, e);
		}
	}

	public boolean pauseVm(JpaVirtualMachine vm) {
		String id = vm.getInfrastructureId();
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_PAUSE, id);
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error attempting to pause VM.  VM=" + vm, e);
		}
	}

	public boolean resumeVm(JpaVirtualMachine vm) {
		String id = vm.getInfrastructureId();
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_RESUME, id);
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error attempting to resume VM.  VM=" + vm, e);
		}
	}

	@SuppressWarnings("unused")
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
	public VmState getVirtualMachineState(JpaVirtualMachine vm) {
		String command = String.format(WINDOWS_PATH_TO_VIRTUAL_BOX + COMMAND + " " + ARGS_GETINFO,
				vm.getInfrastructureId());
		try {
			Process p = Runtime.getRuntime().exec(command);
			Properties props = new Properties();
			props.load(p.getInputStream());
			p.waitFor();
			String state = (String) props.get("VMState");
			state = state.replaceAll("\"", "");
			switch (state) {
			case "running":
				return VmState.RUNNING;
			default:
				logger.warn("Need to define status ot state for virtualbox. vmStatus=" + state);
				return null;
			}
		} catch (IOException | InterruptedException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error attempting to get vm state.  VM=" + vm, e);
		}

	}

	@Override
	public Collection<JpaVirtualMachine> provisionVirtualMachineTemplates(JpaVirtueUser user,
			Collection<JpaVirtualMachineTemplate> vmTemplates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachines(Collection<JpaVirtualMachine> vms) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<JpaVirtualMachine> startVirtualMachines(Collection<JpaVirtualMachine> vms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<JpaVirtualMachine> stopVirtualMachines(Collection<JpaVirtualMachine> vms) {
		// TODO Auto-generated method stub
		return null;
	}

}
