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
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VmState;

public class AwsEc2Wrapper {
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2Wrapper.class);
	private static final int SSH_PORT = 22;

	private AmazonEC2 ec2;
	
	public AwsEc2Wrapper(VirtueAwsEc2Provider ec2Provider) {
		this.ec2=ec2Provider.getEc2();
	}

	public AmazonEC2 getEc2() {
		return ec2;
	}

	public VirtualMachine provisionVm(VirtualMachineTemplate vmt, String namePrefix, Collection<String> securityGroups,
			String serverKeyName, InstanceType instanceType) {
		
		VirtualMachine vm = null; 
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

			String templatePath = vmt.getTemplatePath();
			runInstancesRequest = runInstancesRequest.withImageId(templatePath).withInstanceType(instanceType)
					.withMinCount(1).withMaxCount(1).withKeyName(serverKeyName).withSecurityGroups(securityGroups);
			RunInstancesResult result = ec2.runInstances(runInstancesRequest);

			List<Instance> instances = result.getReservation().getInstances();
			if (instances.size() != 1) {
				throw new RuntimeException("Created more than 1 instance when only 1 was expected!");
			}
			
			Instance instance = instances.get(0);

			String name = namePrefix + instance.getInstanceId();
			String loginUsername = vmt.getLoginUser();
			String privateKeyName = serverKeyName;
			
			vm = new VirtualMachine(UUID.randomUUID().toString(), name,
					new ArrayList<ApplicationDefinition>(vmt.getApplications()), VmState.CREATING, vmt.getOs(),
					instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT, loginUsername, null, privateKeyName,
					instance.getPublicIpAddress());
		return vm;
	}

	public void startVirtualMachines(Collection<VirtualMachine> vms) {
		List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);
		StartInstancesResult result = ec2.startInstances(startInstancesRequest);
		for (InstanceStateChange i : result.getStartingInstances()) {
			for (VirtualMachine vm : vms) {
				if (vm.getInfrastructureId().equals(i.getInstanceId())) {
					vm.setState(VmState.LAUNCHING);
				}
			}
		}
	}

	public void stopVirtualMachines(Collection<VirtualMachine> vms) {
		List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		StopInstancesRequest startInstancesRequest = new StopInstancesRequest(instanceIds);
		StopInstancesResult result = ec2.stopInstances(startInstancesRequest);
		for (InstanceStateChange i : result.getStoppingInstances()) {
			for (VirtualMachine vm : vms) {
				if (vm.getInfrastructureId().equals(i.getInstanceId())) {
					vm.setState(VmState.STOPPING);
				}
			}
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
