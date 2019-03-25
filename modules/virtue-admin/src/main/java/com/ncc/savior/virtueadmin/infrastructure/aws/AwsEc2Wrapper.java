package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.ResourceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

/**
 * Class that essentially wraps the AWS EC2 interface to provide a simpler
 * interface for what the project needs.
 * 
 *
 */
public class AwsEc2Wrapper {
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2Wrapper.class);
	private static final int SSH_PORT = 22;
	public static final String AWS_NOT_FOUND_ERROR_CODE = "InvalidVolume.NotFound";
	public static final String AWS_VOLUME_IN_USE_ERROR_CODE = "VolumeInUse";

	private AmazonEC2 ec2;

	// If null, use subnet default setting. If true, force public ip. If false,
	// force no public ip.
	private Boolean forcePublicIp;
	private String serverId;

	public AwsEc2Wrapper(VirtueAwsEc2Provider ec2Provider, ServerIdProvider serverIdProvider, String forcePublicIp) {
		this.ec2 = ec2Provider.getEc2();
		this.serverId = serverIdProvider.getServerId();
		if (!JavaUtil.isNotEmpty(forcePublicIp) || forcePublicIp.equalsIgnoreCase("default")
				|| forcePublicIp.equalsIgnoreCase("null")) {
			this.forcePublicIp = null;
		} else if (forcePublicIp.equalsIgnoreCase("on") || forcePublicIp.equalsIgnoreCase("true")) {
			this.forcePublicIp = true;
		} else {
			this.forcePublicIp = false;
		}
		logger.debug("ForcePublicIP set to '" + this.forcePublicIp + "' from property=" + forcePublicIp);
	}

	public AmazonEC2 getEc2() {
		return ec2;
	}

	public VirtualMachine provisionVm(VirtualMachineTemplate vmt, String name, Collection<String> securityGroupIds,
			String serverKeyName, InstanceType instanceType, VirtueCreationAdditionalParameters virtueMods,
			String iamRoleName) {

		VirtualMachine vm = null;
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		String templatePath = vmt.getTemplatePath();
		runInstancesRequest = runInstancesRequest.withImageId(templatePath).withInstanceType(instanceType)
				.withMinCount(1).withMaxCount(1).withKeyName(serverKeyName);

		if (forcePublicIp != null) {
			InstanceNetworkInterfaceSpecification networkInterfaces = new InstanceNetworkInterfaceSpecification();
			networkInterfaces.setAssociatePublicIpAddress(forcePublicIp);
			networkInterfaces.setDeviceIndex(0);
			networkInterfaces.setSubnetId(virtueMods.getSubnetId());
			networkInterfaces.setGroups(securityGroupIds);
			runInstancesRequest.withNetworkInterfaces(networkInterfaces);
		} else {
			runInstancesRequest.withSubnetId(virtueMods.getSubnetId()).withSecurityGroupIds(securityGroupIds);
		}

		if (iamRoleName != null) {
			IamInstanceProfileSpecification iamInstanceProfile = new IamInstanceProfileSpecification();
			iamInstanceProfile.setName(iamRoleName);
			runInstancesRequest.withIamInstanceProfile(iamInstanceProfile);
		}
		String instanceId = UUID.randomUUID().toString();
		List<Tag> tags = getTagsFromVirtueMods(vmt.getId(), name, virtueMods, instanceId);
		runInstancesRequest
				.withTagSpecifications(new TagSpecification().withResourceType(ResourceType.Instance).withTags(tags));
		// .withSecurityGroups(securityGroups);
		RunInstancesResult result = ec2.runInstances(runInstancesRequest);

		List<Instance> instances = result.getReservation().getInstances();
		if (instances.size() != 1) {
			throw new RuntimeException("Created more than 1 instance when only 1 was expected!");
		}

		Instance instance = instances.get(0);

		String loginUsername = vmt.getLoginUser();
		String privateKeyName = serverKeyName;

		vm = new VirtualMachine(instanceId, name, new ArrayList<ApplicationDefinition>(vmt.getApplications()),
				VmState.CREATING, vmt.getOs(), instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT,
				loginUsername, null, privateKeyName, instance.getPublicIpAddress());
		return vm;
	}

	public List<Tag> getTagsFromVirtueMods(String vmtId, String name, VirtueCreationAdditionalParameters virtueMods,
			String instanceId) {
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(AwsUtil.TAG_SERVER_ID, serverId));
		tags.add(new Tag(AwsUtil.TAG_NAME, name));
		if (vmtId != null) {
			tags.add(new Tag(AwsUtil.TAG_VM_TEMPLATE_ID, vmtId));
		}
		if (instanceId != null) {
			tags.add(new Tag(AwsUtil.TAG_VM_INSTANCE_ID, instanceId));
		}
		if (virtueMods != null) {
			if (virtueMods.getVirtueId() != null) {
				tags.add(new Tag(AwsUtil.TAG_VIRTUE_INSTANCE_ID, virtueMods.getVirtueId()));
			}
			if (virtueMods.getVirtueTemplateId() != null) {
				tags.add(new Tag(AwsUtil.TAG_VIRTUE_TEMPLATE_ID, virtueMods.getVirtueTemplateId()));
			}
			if (virtueMods.getPrimaryPurpose() != null) {
				tags.add(new Tag(AwsUtil.TAG_PRIMARY, virtueMods.getPrimaryPurpose().toString()));
			}
			if (virtueMods.getSecondaryPurpose() != null) {
				tags.add(new Tag(AwsUtil.TAG_SECONDARY, virtueMods.getSecondaryPurpose().toString()));
			}
			if (virtueMods.getUsername() != null) {
				tags.add(new Tag(AwsUtil.TAG_USERNAME, virtueMods.getUsername()));
			}
		}
		return tags;
	}

	public void startVirtualMachines(Collection<VirtualMachine> vms) {
		List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);
		try {
			StartInstancesResult result = ec2.startInstances(startInstancesRequest);
			for (InstanceStateChange i : result.getStartingInstances()) {
				for (VirtualMachine vm : vms) {
					if (vm.getInfrastructureId().equals(i.getInstanceId())) {
						vm.setState(VmState.LAUNCHING);
					}
				}
			}
		} catch (Exception e) {
			String msg = "error starting virtual machines = " + vms;
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.AWS_ERROR, msg, e);
		}
	}

	public void stopVirtualMachines(Collection<VirtualMachine> vms) {
		List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);
		stopInstancesRequest.withForce(true);
		try {
			StopInstancesResult result = ec2.stopInstances(stopInstancesRequest);
			for (InstanceStateChange i : result.getStoppingInstances()) {
				for (VirtualMachine vm : vms) {
					if (vm.getInfrastructureId().equals(i.getInstanceId())) {
						vm.setState(VmState.STOPPING);
					}
				}
			}
		} catch (Exception e) {
			String msg = "error stopping virtual machines = " + vms;
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.AWS_ERROR, msg, e);
		}
	}

	public Collection<VirtualMachine> deleteVirtualMachines(Collection<VirtualMachine> vms) {
		Collection<VirtualMachine> terminatedVms = new ArrayList<VirtualMachine>();
		try {
			List<String> instanceIds = new ArrayList<String>(vms.size());
			for (VirtualMachine vm : vms) {
				instanceIds.add(vm.getInfrastructureId());
			}
			TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
			TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
			if (logger.isTraceEnabled()) {
				logger.trace("Terminating: " + result.getTerminatingInstances());
			}
			for (InstanceStateChange r : result.getTerminatingInstances()) {
				String id = r.getInstanceId();
				for (VirtualMachine vm : vms) {
					if (vm.getInfrastructureId().equals(id)) {
						terminatedVms.add(vm);
						vm.setState(VmState.DELETING);
					}
				}
			}
			return terminatedVms;
		} catch (AmazonEC2Exception e) {
			logger.warn("Error terminating instances", e);
		}
		return terminatedVms;
	}

	public VmState getVirtualMachineState(VirtualMachine vm) {
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		Collection<String> instanceIds = new ArrayList<String>(1);
		instanceIds.add(vm.getInfrastructureId());
		describeInstanceStatusRequest.setInstanceIds(instanceIds);
		DescribeInstanceStatusResult statusResult = ec2.describeInstanceStatus(describeInstanceStatusRequest);
		Iterator<InstanceStatus> itr = statusResult.getInstanceStatuses().iterator();
		InstanceStatus awsStatus = itr.next();
		return AwsUtil.awsStatusToSaviorState(awsStatus);
	}
}
