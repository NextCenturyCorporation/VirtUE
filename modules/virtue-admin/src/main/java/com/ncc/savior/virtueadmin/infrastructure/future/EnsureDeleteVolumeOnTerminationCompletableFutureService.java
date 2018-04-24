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
 * 
 */
public class EnsureDeleteVolumeOnTerminationCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory
			.getLogger(EnsureDeleteVolumeOnTerminationCompletableFutureService.class);
	private AmazonEC2 ec2;

	public EnsureDeleteVolumeOnTerminationCompletableFutureService(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, true, 1000, 3000);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		try {
			boolean success = ensureBlockDevisesDeletedOnTermination(vm.getInfrastructureId());
			if (success) {
				onSuccess(vm.getId(), vm, wrapper.future);
			}
		} catch (Exception e) {
			logger.trace("Naming in AWS failed for vm=" + vm.getId());
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
			Boolean delOnTerm = mapping.getEbs().getDeleteOnTermination();
			if (!delOnTerm) {
				InstanceBlockDeviceMappingSpecification mapspec = new InstanceBlockDeviceMappingSpecification();
				EbsInstanceBlockDeviceSpecification ebs = new EbsInstanceBlockDeviceSpecification();
				ebs.setDeleteOnTermination(true);
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
