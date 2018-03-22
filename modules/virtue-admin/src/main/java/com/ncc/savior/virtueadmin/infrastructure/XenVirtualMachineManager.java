/* 
*  XenLibvirtManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Mar 8, 2018
*  
*  Copyright (c) 2018 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;

public class XenVirtualMachineManager implements IVmManager {

	
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = "VRTU-";
	private String privateKey;
	private SshKeyInjector sshKeyInjector;
	private String serverUser;
	private String defaultLoginUsername;

	private String defaultTemplate; 
	
	
	public XenVirtualMachineManager() {
		// TODO Auto-generated constructor stub
	}

	public XenVirtualMachineManager(File privatekeyfile) {

		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);
		this.defaultLoginUsername = "admin";
		this.sshKeyInjector = new SshKeyInjector();
		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);
		this.serverUser = System.getProperty("user.name");
	}


	@Override
	public void addStateUpdateListener(IStateUpdateListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeStateUpdateListener(IStateUpdateListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user, VirtualMachineTemplate vmt) {
		
		int i = 0; 
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VmState getVirtualMachineState(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub
		
	}

}
