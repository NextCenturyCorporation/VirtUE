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
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDeviceSpecification;
import com.amazonaws.services.ec2.model.InstanceAttributeName;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMappingSpecification;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeResult;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * This service ensures that AWS Virtual Machines have their volumnes set to
 * delete when the VM terminates. If this is not set, the volumes will stay
 * around and cost money for storage.
 */
public class EnsureDeleteVolumeOnTerminationCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory
			.getLogger(EnsureDeleteVolumeOnTerminationCompletableFutureService.class);
	private AmazonEC2 ec2;
	private String persistentDeviceName;

	public EnsureDeleteVolumeOnTerminationCompletableFutureService(ScheduledExecutorService executor, AmazonEC2 ec2,
			int timeoutMillis, String persistentDeviceName) {
		super(executor, true, 10, 1000, timeoutMillis);
		this.ec2 = ec2;
		this.persistentDeviceName = persistentDeviceName;
	}

	@Override
	protected void onExecute(String id, Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		try {
			boolean success = ensureBlockDevisesDeletedOnTermination(vm.getInfrastructureId());
			if (success) {
				onSuccess(id, vm, wrapper.future);
			}
		} catch (Exception e) {
			logger.warn("Naming in AWS failed for vm=" + vm.getId());
			return;
		}
	}

	private boolean ensureBlockDevisesDeletedOnTermination(String instanceId) {
		DescribeInstanceAttributeRequest diar = new DescribeInstanceAttributeRequest(instanceId,
				InstanceAttributeName.BlockDeviceMapping);
		DescribeInstanceAttributeResult diaResult = ec2.describeInstanceAttribute(diar);
		List<InstanceBlockDeviceMapping> mappings = diaResult.getInstanceAttribute().getBlockDeviceMappings();
		Collection<InstanceBlockDeviceMappingSpecification> mapspecs = new ArrayList<InstanceBlockDeviceMappingSpecification>();
		// List<InstanceBlockDeviceMapping> mappings =
		// instance.getBlockDeviceMappings();
		if (mappings.isEmpty()) {
			return false;
		}
		for (InstanceBlockDeviceMapping mapping : mappings) {
			boolean delOnTerm = mapping.getEbs().getDeleteOnTermination();
			boolean shouldBeDeletedOnTerm = !(persistentDeviceName.equals(mapping.getDeviceName()));
			if (delOnTerm != shouldBeDeletedOnTerm) {
				InstanceBlockDeviceMappingSpecification mapspec = new InstanceBlockDeviceMappingSpecification();
				EbsInstanceBlockDeviceSpecification ebs = new EbsInstanceBlockDeviceSpecification();
				ebs.setDeleteOnTermination(shouldBeDeletedOnTerm);
				ebs.setVolumeId(mapping.getEbs().getVolumeId());
				mapspec.setEbs(ebs);
				mapspec.setDeviceName(mapping.getDeviceName());
				mapspecs.add(mapspec);
			}
		}
		if (!mapspecs.isEmpty()) {
			ModifyInstanceAttributeRequest mod = new ModifyInstanceAttributeRequest();
			mod.setInstanceId(instanceId);
			mod.setAttribute(InstanceAttributeName.BlockDeviceMapping);
			mod.setBlockDeviceMappings(mapspecs);
			ModifyInstanceAttributeResult modResult = ec2.modifyInstanceAttribute(mod);
			if (modResult.getSdkHttpMetadata().getHttpStatusCode() >= 400) {
				return false;
			}
		} else {
			logger.trace("All EBS is set to delete on termination");
		}
		return true;
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
		return "EnsureDeleteVolumeTerminationService";
	}

}
