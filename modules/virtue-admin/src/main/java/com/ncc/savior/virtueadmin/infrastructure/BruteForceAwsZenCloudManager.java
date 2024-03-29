/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.InstanceType;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.cifsproxy.CifsManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;

public class BruteForceAwsZenCloudManager implements ICloudManager {

	private AmazonEC2 ec2;
	private InstanceType instanceType;
	private List<String> defaultSecurityGroups;
	private String serverKeyName;
	private VirtueUser xenUser;
	private VirtualMachineTemplate xenVmt;
	private InstanceType xenInstanceType;
	private Collection<String> xenSecurityGroups;
	private String xenServerKeyName;
	private IKeyManager keyManager;
	private SshKeyInjector sshKeyInjector;
	private boolean usePublicDns = true;

	public BruteForceAwsZenCloudManager(VirtueAwsEc2Provider ec2Provider, IKeyManager keyManager, String xenAmi,
			String vmKeyName, List<String> vmSecurityGroups, String xenKeyName) {
		this.ec2 = ec2Provider.getEc2();
		this.instanceType = InstanceType.T2Small;
		this.defaultSecurityGroups = vmSecurityGroups;
		this.serverKeyName = vmKeyName;
		this.xenUser = new VirtueUser("XenUser", new ArrayList<String>(), true);
		this.xenVmt = new VirtualMachineTemplate(UUID.randomUUID().toString(), "ZenTemplate", OS.LINUX, xenAmi,
				new ArrayList<ApplicationDefinition>(), "admin", false, new Date(0), "system");
		// TODO redo xen config
		this.xenInstanceType = InstanceType.T2Small;
		this.xenSecurityGroups = vmSecurityGroups;
		this.xenServerKeyName = xenKeyName;
		this.keyManager = keyManager;
		this.sshKeyInjector = new SshKeyInjector();
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future) {
		// TODO Auto-generated method stub

	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> linuxVmts = new ArrayList<VirtualMachineTemplate>();
		Collection<VirtualMachineTemplate> windowsVmts = new ArrayList<VirtualMachineTemplate>();
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		for (VirtualMachineTemplate vmt : vmts) {
			if (OS.LINUX.equals(vmt.getOs())) {
				linuxVmts.add(vmt);
			} else if (OS.WINDOWS.equals(vmt.getOs())) {
				windowsVmts.add(vmt);
			}
		}

		VirtueInstance vi = new VirtueInstance(template, user.getUsername());
		Collection<VirtualMachine> vms = vi.getVms();
		VirtualMachine zenVm = AwsUtil.provisionVm(ec2, xenUser, xenVmt, getXenName(vi, template), xenInstanceType,
				xenSecurityGroups, xenServerKeyName);

		for (VirtualMachineTemplate vmt : windowsVmts) {
			String namePrefix = getNamePrefix(user, vmt);
			VirtualMachine vm = AwsUtil.provisionVm(ec2, user, vmt, namePrefix, instanceType, defaultSecurityGroups,
					serverKeyName);
			vms.add(vm);
		}

		Collection<VirtualMachine> allVms = new ArrayList<VirtualMachine>();
		allVms.addAll(vms);
		allVms.add(zenVm);

		AwsUtil.waitUntilAllNetworkingUpdated(ec2, allVms, 2000, usePublicDns);
		SshUtil.waitForAllVmsReachableParallel(vms, 5000);

		for (VirtualMachine vm : allVms) {
			File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
			String newPrivateKey = sshKeyInjector.injectSshKey(vm, privateKeyFile);
			vm.setPrivateKey(newPrivateKey);
		}

		// TODO use zenVm to
		return vi;
	}

	private String getXenName(VirtueInstance vi, VirtueTemplate template) {
		return vi.getId();
	}

	private String getNamePrefix(VirtueUser user, VirtualMachineTemplate vmt) {
		String serverUser = System.getProperty("user.name");
		return "VRTU-" + serverUser + "-" + user.getUsername();
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rebootVm(VirtualMachine vm, String virtue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sync(List<String> ids) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "Sync not implemented in this implementation");
	}

	@Override
	public void setCifsManager(CifsManager cifsManager) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "CIFS not implemented in this implementation");
		
		
	}

}
