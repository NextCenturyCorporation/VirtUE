package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Virtual machine manager that assumes it has a single VM which will fulfill
 * all templates and is already running.
 * 
 * See interface for function comments.
 */
public class StaticMachineVmManager extends BaseVmManager implements IVmManager {
	private String hostname;
	private int sshPort;
	private OS os;

	public StaticMachineVmManager(String hostname, int sshPort, OS os) {
		this.hostname = hostname;
		this.sshPort = sshPort;
		this.os = os;
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtualMachineTemplate vmt) {
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
				VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort);
		return vm;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm) {
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm) {
		// Do nothing. we don't want to delete in this implementation.
	}

	@Override
	public VmState getVirtialMachineState(VirtualMachine vm) {
		return VmState.RUNNING;
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(
			Collection<VirtualMachineTemplate> vmTemplates) {
		Collection<VirtualMachine> vms = new HashSet<VirtualMachine>();
		for (VirtualMachineTemplate vmt : vmTemplates) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
					VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort);
			vms.add(vm);
		}
		return vms;
	}

}
