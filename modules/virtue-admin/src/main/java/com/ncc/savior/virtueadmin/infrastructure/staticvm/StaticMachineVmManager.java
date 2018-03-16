package com.ncc.savior.virtueadmin.infrastructure.staticvm;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.BaseVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;
import com.ncc.savior.virtueadmin.util.SshUtil;

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
	public JpaVirtualMachine provisionVirtualMachineTemplate(JpaVirtueUser user, JpaVirtualMachineTemplate vmt) {
		JpaVirtualMachine vm = new JpaVirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
				VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort, userName, privateKey, null,
				ipAddress);
		return vm;
	}

	@Override
	public JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm) {
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public JpaVirtualMachine stopVirtualMachine(JpaVirtualMachine vm) {
		vm.setState(VmState.RUNNING);
		return vm;
	}

	@Override
	public void deleteVirtualMachine(JpaVirtualMachine vm) {
		// Do nothing. we don't want to delete in this implementation.
	}

	@Override
	public VmState getVirtualMachineState(JpaVirtualMachine vm) {
		return VmState.RUNNING;
	}

	@Override
	public Collection<JpaVirtualMachine> provisionVirtualMachineTemplates(JpaVirtueUser user,
			Collection<JpaVirtualMachineTemplate> vmTemplates) {
		Collection<JpaVirtualMachine> vms = new HashSet<JpaVirtualMachine>();
		for (JpaVirtualMachineTemplate vmt : vmTemplates) {
			JpaVirtualMachine vm = new JpaVirtualMachine(UUID.randomUUID().toString(), vmt.getName(),
					vmt.getApplications(),
					VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort, user.getUsername(),
					privateKey, null, ipAddress);
			vms.add(vm);

		}
		logger.debug("Pretending to provision " + vms.size() + " VMs from a single VM.");
		return vms;
	}

	@Override
	public void deleteVirtualMachines(Collection<JpaVirtualMachine> vms) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Collection<JpaVirtualMachine> startVirtualMachines(Collection<JpaVirtualMachine> vms) {
		for (JpaVirtualMachine vm : vms) {
			startVirtualMachine(vm);
		}
		return vms;
	}

	@Override
	public Collection<JpaVirtualMachine> stopVirtualMachines(Collection<JpaVirtualMachine> vms) {
		for (JpaVirtualMachine vm : vms) {
			stopVirtualMachine(vm);
		}
		return vms;
	}

}
