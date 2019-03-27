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
package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.BaseGroupedVmPipelineComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.PipelineWrapper;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * {@link BaseGroupedVmPipelineComponent} that updates the status of a VM based
 * on the status from AWS.
 * 
 *
 */
public class AwsUpdateStatus extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	private Collection<VmState> successStatus;
	private AmazonEC2 ec2;

	public AwsUpdateStatus(ScheduledExecutorService executor, AmazonEC2 ec2, Collection<VmState> successStatus) {
		super(executor, true, 2000, 2500);
		this.ec2 = ec2;
		this.successStatus = successStatus;
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		try {
			AwsUtil.updateStatusOnVms(ec2, unwrap(wrappers));
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
				doOnFailure(wrappers);
			}
		}
		Iterator<PipelineWrapper<VirtualMachine>> itr = wrappers.iterator();
		while (itr.hasNext()) {
			PipelineWrapper<VirtualMachine> wrapper = itr.next();
			if (!successStatus.contains(wrapper.get().getState())) {
				// remove all of the unsuccessful statuses
				itr.remove();
			}
		}
		doOnSuccess(wrappers);
	}
}
