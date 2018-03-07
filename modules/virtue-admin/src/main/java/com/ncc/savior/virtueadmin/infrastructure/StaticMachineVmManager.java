package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;

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
		this(hostname, sshPort, userName, getKeyFromFile(privateKey), os);
	}

	static String getKeyFromFile(File privateKey) {
		FileReader reader = null;
		if (privateKey == null || !privateKey.isFile()) {
			return "";
		}
		try {
			reader = new FileReader(privateKey);
			char[] cbuf = new char[4096];
			int n = reader.read(cbuf);
			String s = new String(cbuf, 0, n);
			return s;
		} catch (IOException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR,
					"Error attempting to read file=" + privateKey.getAbsolutePath(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("Error attempting to close file=" + privateKey.getAbsolutePath());
				}
			}
		}
	}

	public StaticMachineVmManager(String hostname, int sshPort, String userName, String privateKey, OS os) {
		this.hostname = hostname;
		this.sshPort = sshPort;
		this.os = os;
		this.userName = userName;
		this.privateKey = privateKey;
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user, VirtualMachineTemplate vmt) {
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
				VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort, userName, privateKey,
				ipAddress);
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
	public VmState getVirtualMachineState(VirtualMachine vm) {
		return VmState.RUNNING;
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates) {
		Collection<VirtualMachine> vms = new HashSet<VirtualMachine>();
		for (VirtualMachineTemplate vmt : vmTemplates) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), vmt.getName(), vmt.getApplications(),
					VmState.RUNNING, os, UUID.randomUUID().toString(), hostname, sshPort, user.getUsername(),
					privateKey, ipAddress);
			vms.add(vm);

		}
		return vms;
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not implemented");
	}

}
