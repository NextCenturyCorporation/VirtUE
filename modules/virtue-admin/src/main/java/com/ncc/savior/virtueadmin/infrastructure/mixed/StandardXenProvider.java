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
package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.ec2.model.InstanceType;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

public class StandardXenProvider implements IXenVmProvider {
	private IVpcSubnetProvider vpcSubnetProvider;
	private AwsEc2Wrapper ec2Wrapper;
	private VirtualMachineTemplate xenVmTemplate;
	private String iamRoleName;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private String serverId;

	public StandardXenProvider(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper,
			IVpcSubnetProvider vpcSubnetProvider, String xenAmi, String xenLoginUser, String xenKeyName,
			InstanceType xenInstanceType, String iamRoleName) {
		this.serverId = serverIdProvider.getServerId();
		this.ec2Wrapper = ec2Wrapper;
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.xenKeyName = xenKeyName;
		this.iamRoleName = iamRoleName;
		this.xenInstanceType = xenInstanceType;
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX, xenAmi,
				new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), "system");
	}

	public StandardXenProvider(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper,
			IVpcSubnetProvider vpcSubnetProvider, String xenAmi, String xenLoginUser, String xenKeyName,
			String xenInstanceType, String iamRoleName) {
		this(serverIdProvider, ec2Wrapper, vpcSubnetProvider, xenAmi, xenLoginUser, xenKeyName,
				InstanceType.fromValue(xenInstanceType), iamRoleName);
	}

	@Override
	public VirtualMachine getNewXenVm(VirtueInstance vi, VirtueCreationAdditionalParameters virtueMods,
			String virtueName, Collection<String> secGroupIds) {

		Map<String, String> tags = new HashMap<String, String>();
		tags.put(AwsUtil.TAG_USERNAME, vi.getUsername());
		tags.put(AwsUtil.TAG_VIRTUE_NAME, vi.getName());
		tags.put(AwsUtil.TAG_VIRTUE_INSTANCE_ID, vi.getId());
		tags.put(AwsUtil.TAG_VIRTUE_TEMPLATE_ID, vi.getTemplateId());

		String subnetId = vpcSubnetProvider.getSubnetId(vi.getId(), tags);
		virtueMods.setSubnetId(subnetId);

		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate,
				"VRTU-Xen-" + serverId + "-" + vi.getUsername() + "-" + virtueName, secGroupIds, xenKeyName,
				xenInstanceType, virtueMods, iamRoleName);
		return xenVm;
	}

	@Override
	public void setXenPoolSize(int poolSize) {
		throw new SaviorException(SaviorErrorCode.CONFIGURATION_ERROR,
				"Unable to change pool size of Standard Xen Provider.");
	}

	@Override
	public int getXenPoolSize() {
		return 0;
	}
}
