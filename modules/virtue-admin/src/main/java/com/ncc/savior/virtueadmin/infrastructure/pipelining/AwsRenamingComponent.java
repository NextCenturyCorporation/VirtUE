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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Component of an {@link IUpdatePipeline} that will rename a VM instance in
 * AWS. The name is based on {@link VirtualMachine#getName()}.
 */
public class AwsRenamingComponent extends BaseIndividualVmPipelineComponent<VirtualMachine> {
	private static final Logger logger = LoggerFactory.getLogger(AwsRenamingComponent.class);
	private AmazonEC2 ec2;

	public AwsRenamingComponent(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, true, 1000, 3000);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(PipelineWrapper<VirtualMachine> wrapper) {
		VirtualMachine vm = wrapper.get();
		try {
			nameVmInAws(vm);
			doOnSuccess(wrapper);
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
	 */
	private void nameVmInAws(VirtualMachine vm) {
		CreateTagsRequest ctr = new CreateTagsRequest();
		ctr.withResources(vm.getInfrastructureId());
		Collection<Tag> tags = new ArrayList<Tag>();
		// TODO ?? tags.add(new Tag("Autogen-Virtue-VM", serverUser));
		tags.add(new Tag("Name", vm.getName()));
		ctr.setTags(tags);
		ec2.createTags(ctr);
	}

	@Override
	protected String getId(VirtualMachine element) {
		return element.getId();
	}
}
