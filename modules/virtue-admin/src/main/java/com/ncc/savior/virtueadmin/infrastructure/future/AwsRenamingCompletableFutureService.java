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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Service which will try to rename an AWS machine based on the name in the
 * {@link VirtualMachine}. This service will retry until successful and then
 * complete the future.
 * 
 *
 */
public class AwsRenamingCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory.getLogger(AwsRenamingCompletableFutureService.class);
	private AmazonEC2 ec2;

	public AwsRenamingCompletableFutureService(ScheduledExecutorService executor, AmazonEC2 ec2,
			int timeoutMillis) {
		super(executor, true, 10, 3000, timeoutMillis);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(String id, Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		try {
			boolean success = nameVmInAws(vm);
			if (success) {
				onSuccess(id, vm, wrapper.future);
			}
		} catch (Exception e) {
			logger.trace("Naming in AWS failed for vm=" + vm.getId());
			return;
		}
	}

	/**
	 * Renames the VM based on the {@link VirtualMachine#getName()} method. The name
	 * is set earlier in the provision process.
	 * 
	 * @param vm
	 * @return
	 */
	private boolean nameVmInAws(VirtualMachine vm) {
		CreateTagsRequest ctr = new CreateTagsRequest();
		ctr.withResources(vm.getInfrastructureId());
		Collection<Tag> tags = new ArrayList<Tag>();
		// TODO ?? tags.add(new Tag("Autogen-Virtue-VM", serverUser));
		tags.add(new Tag("Name", vm.getName()));
		ctr.setTags(tags);
		CreateTagsResult result = ec2.createTags(ctr);
		return result.getSdkHttpMetadata().getHttpStatusCode() < 300;
	}

	@Override
	protected String getId(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return "AwsRenamingService";
	}

}
