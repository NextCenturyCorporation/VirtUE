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
package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Component of an {@link IUpdatePipeline} that will retrieve the networking (IP
 * address and hostname) from AWS and add it to the {@link VirtualMachine}
 * object.
 */
public class AwsNetworkingUpdateComponent extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	private AmazonEC2 ec2;
	private boolean usePublicDns;

	public AwsNetworkingUpdateComponent(ScheduledExecutorService executor, AmazonEC2 ec2, boolean usePublicDns) {
		super(executor, false, 1000, 5000);
		this.ec2 = ec2;
		this.usePublicDns=usePublicDns;
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		ArrayList<PipelineWrapper<VirtualMachine>> updated = new ArrayList<PipelineWrapper<VirtualMachine>>();
		AwsUtil.updateNetworking(ec2, unwrap(wrappers), usePublicDns);
		for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
			VirtualMachine vm = wrapper.get();
			if (JavaUtil.isNotEmpty(vm.getHostname())) {
				vm.setState(VmState.LAUNCHING);
				updated.add(wrapper);
			}
		}
		if (!updated.isEmpty()) {
			doOnSuccess(updated);
		}
	}

}
