package com.ncc.savior.virtueadmin.infrastructure.pipelining;

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
 * Component of an {@link IUpdatePipeline} that change AWS volumes to be deleted
 * when the instance is deleted for a {@link VirtualMachine} object.
 */
public class EnsureDeleteVolumeOnTerminationUpdateComponent extends BaseIndividualVmPipelineComponent<VirtualMachine> {
	private static final Logger logger = LoggerFactory.getLogger(EnsureDeleteVolumeOnTerminationUpdateComponent.class);
	private AmazonEC2 ec2;

	public EnsureDeleteVolumeOnTerminationUpdateComponent(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, false, 1000, 5000);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(PipelineWrapper<VirtualMachine> pipelineWrapper) {
		ensureBlockDevisesDeletedOnTermination(pipelineWrapper.get().getInfrastructureId());
		doOnSuccess(pipelineWrapper);
	}

	private void ensureBlockDevisesDeletedOnTermination(String instanceId) {
		DescribeInstanceAttributeRequest diar = new DescribeInstanceAttributeRequest(instanceId,
				InstanceAttributeName.BlockDeviceMapping);
		DescribeInstanceAttributeResult diaResult = ec2.describeInstanceAttribute(diar);
		List<InstanceBlockDeviceMapping> mappings = diaResult.getInstanceAttribute().getBlockDeviceMappings();
		Collection<InstanceBlockDeviceMappingSpecification> mapspecs = new ArrayList<InstanceBlockDeviceMappingSpecification>();
		// List<InstanceBlockDeviceMapping> mappings =
		// instance.getBlockDeviceMappings();
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
		} else {
			logger.trace("All EBS is set to delete on termination");
		}

	}

	@Override
	protected String getId(VirtualMachine element) {
		return element.getId();
	}
}