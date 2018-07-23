package com.ncc.savior.virtueadmin.infrastructure.staticvm;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.BaseVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Virtual machine manager that assumes it has a single VM which will fulfill
 * all templates and is already running.
 * 
 * See interface for function comments.
 */
public class StaticMachineVmManager extends BaseVmManager implements IVmManager {
	private static Logger logger = LoggerFactory.getLogger(StaticMachineVmManager.class);
	private String hostname;
	private int sshPort;
	private OS os;
	private String userName;
	private String privateKey;
	private String ipAddress;

	public StaticMachineVmManager(String hostname, int sshPort, String userName, File privateKey, OS os) {
		this(hostname, sshPort, userName, SshUtil.getKeyFromFile(privateKey), os);
	}

	public StaticMachineVmManager(String hostname, int sshPort, String userName, String privateKey, OS os) {
		this.hostname = hostname;
		this.sshPort = sshPort;
		this.os = os;
		this.userName = userName;
		this.privateKey = privateKey;
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user, VirtualMachineTemplate vmt,
			CompletableFuture<Collection<VirtualMachine>> future) {
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
				VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort, userName, privateKey, null,
				ipAddress);
		return vm;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> future) {
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> future) {
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> future) {
		// Do nothing. we don't want to delete in this implementation.
	}

	@Override
	public VmState getVirtualMachineState(VirtualMachine vm) {
		return VmState.RUNNING;
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates, CompletableFuture<Collection<VirtualMachine>> future, String virtue) {
		Collection<VirtualMachine> vms = new HashSet<VirtualMachine>();
		for (VirtualMachineTemplate vmt : vmTemplates) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
					VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort, user.getUsername(),
					privateKey, null, ipAddress);
			vms.add(vm);

		}
		logger.debug("Pretending to provision " + vms.size() + " VMs from a single VM.");
		return vms;
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Collection<VirtualMachine> startVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		for (VirtualMachine vm : vms) {
			startVirtualMachine(vm, future);
		}
		return vms;
	}

	@Override
	public Collection<VirtualMachine> stopVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		for (VirtualMachine vm : vms) {
			stopVirtualMachine(vm, future);
		}
		return vms;
	}

}
