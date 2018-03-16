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

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2VmManager;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

public class XenVirtualMachineManager extends BaseVmManager {

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

		this.privateKey = SshUtil.getKeyFromFile(privatekeyfile);
		this.defaultLoginUsername = "admin";
		this.sshKeyInjector = new SshKeyInjector();
		this.privateKey = SshUtil.getKeyFromFile(privatekeyfile);
		this.serverUser = System.getProperty("user.name");
	}

	@Override
	public JpaVirtualMachine provisionVirtualMachineTemplate(JpaVirtueUser user, JpaVirtualMachineTemplate vmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JpaVirtualMachine stopVirtualMachine(JpaVirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachine(JpaVirtualMachine vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public VmState getVirtualMachineState(JpaVirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
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
