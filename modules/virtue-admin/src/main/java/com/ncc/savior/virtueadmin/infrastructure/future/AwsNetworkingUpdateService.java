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
package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Service which will try get the networking information for an AWS instance. If
 * usePublicDns is true, it will get the public DNS and IP, otherwise, it will
 * only get the internal hostname and IP. AWS internal DNS and IP are always
 * retrieved and stored in the {@link VirtualMachine}s internal DNS and IP
 * variables. This service will retry until successful and then complete the
 * future.
 * 
 *
 */
public class AwsNetworkingUpdateService
		extends BaseGroupedScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {

	private AmazonEC2 ec2;
	private boolean usePublicDns;

	public AwsNetworkingUpdateService(ScheduledExecutorService executor, AmazonEC2 ec2, boolean usePublicDns,
			int timeoutMillis) {
		super(executor, false, 1000, 5000, timeoutMillis);
		this.ec2 = ec2;
		this.usePublicDns = usePublicDns;
	}

	@Override
	protected void onExecute(
			Collection<BaseGroupedScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper> wrappers) {
		ArrayList<VirtualMachine> updated = new ArrayList<VirtualMachine>();
		Collection<VirtualMachine> vms = unwrapParameter(wrappers);
		AwsUtil.updateNetworking(ec2, vms, usePublicDns);
		for (VirtualMachine vm : vms) {
			if (JavaUtil.isNotEmpty(vm.getHostname())) {
				updated.add(vm);
			}
		}
		ArrayList<BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper> completedWrappers = new ArrayList<Wrapper>();
		if (!updated.isEmpty()) {
			for (VirtualMachine updatedVm : updated) {
				for (BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper : wrappers) {
					if (wrapper.param.getId().equals(updatedVm.getId())) {
						wrapper.result = updatedVm;
						completedWrappers.add(wrapper);
					}
				}
			}
		}
		onSuccess(completedWrappers);
	}

	@Override
	protected String getServiceName() {
		return "AwsNetworkingUpdaterService";
	}

}
