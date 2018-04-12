package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceStatusSummary;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;
import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * Utility functions for managing and updating AWS EC2 VMs.
 *
 */
public class AwsUtil {
	private static final Logger logger = LoggerFactory.getLogger(AwsUtil.class);

	public static Collection<VirtualMachine> updateStatusOnVms(AmazonEC2 ec2, Collection<VirtualMachine> vms) {
		Map<String, VirtualMachine> instanceIdsToVm = new HashMap<String, VirtualMachine>();
		for (VirtualMachine vm : vms) {
			instanceIdsToVm.put(vm.getInfrastructureId(), vm);
		}
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.setInstanceIds(instanceIdsToVm.keySet());
		DescribeInstanceStatusResult statusResult = ec2.describeInstanceStatus(describeInstanceStatusRequest);
		Iterator<InstanceStatus> itr = statusResult.getInstanceStatuses().iterator();
		while (itr.hasNext()) {
			InstanceStatus status = itr.next();
			VirtualMachine vm = instanceIdsToVm.get(status.getInstanceId());
			vm.setState(awsStatusToSaviorState(status));
		}
		return vms;
	}

	public static VirtualMachine updateStatusOnVm(AmazonEC2 ec2, VirtualMachine vm) {
		Map<String, VirtualMachine> instanceIdsToVm = new HashMap<String, VirtualMachine>();
		instanceIdsToVm.put(vm.getInfrastructureId(), vm);
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.setInstanceIds(instanceIdsToVm.keySet());
		DescribeInstanceStatusResult statusResult = ec2.describeInstanceStatus(describeInstanceStatusRequest);
		Iterator<InstanceStatus> itr = statusResult.getInstanceStatuses().iterator();
		if (itr.hasNext()) {
			InstanceStatus status = itr.next();
			vm.setState(awsStatusToSaviorState(status));
		} else {
			vm.setState(VmState.ERROR);
		}
		return vm;
	}

	public static VmState awsStatusToSaviorState(InstanceStatus status) {
		InstanceState vmState = status.getInstanceState();
		InstanceStatusSummary vmStatus = status.getInstanceStatus();
		Integer stateCode = vmState.getCode();

		// 0 : pending
		// 16 : running
		// 32 : shutting-down
		// 48 : terminated
		// 64 : stopping
		// 80 : stopped

		switch (stateCode) {
		case 0: // pending
			return VmState.CREATING;
		case 16: // running
			return getRunningStateFromStatus(vmStatus, status.getInstanceId());
		case 32: // shutting-down
			return VmState.STOPPING;
		case 48: // terminated
			return VmState.DELETING;
		case 64: // stopping
			return VmState.STOPPING;
		case 80: // stopped
			return VmState.STOPPED;
		default:
			logger.error("Unknown state code from AWS. Code=" + stateCode + " instanceId=" + status.getInstanceId());
		}
		return VmState.ERROR;
	}

	public static VmState getRunningStateFromStatus(InstanceStatusSummary vmStatus, String instanceId) {
		switch (vmStatus.getStatus()) {
		case "ok":
			return VmState.RUNNING;
		case "initializing":
			return VmState.LAUNCHING;
		default:
			logger.error("Unknown status from AWS. Code=" + vmStatus.getStatus() + " instanceId=" + instanceId);
		}
		// }
		return VmState.ERROR;
	}

	public static boolean areAllVmsRunning(Collection<VirtualMachine> vms, boolean throwOnErrorState) {
		logger.trace("Checking status of VMs:");
		for (VirtualMachine vm : vms) {
			logger.trace("  " + vm.toString());
			VmState state = vm.getState();
			if (VmState.RUNNING.equals(state)) {
				continue;
			} else if (throwOnErrorState && (state == null || VmState.ERROR.equals(state))) {
				throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Vm state is in error.  VM=" + vm);
			} else {
				return false;
			}
		}
		return true;
	}

	public static void waitForAllVmsRunning(AmazonEC2 ec2, Collection<VirtualMachine> vms,
			long vmStatePollPeriodMillis) {
		boolean throwOnErrorState = true;

		while (!areAllVmsRunning(vms, throwOnErrorState)) {
			try {
				Thread.sleep(vmStatePollPeriodMillis);
			} catch (InterruptedException e) {
				logger.error("Poll sleep interrupted!");
			}
			vms = updateStatusOnVms(ec2, vms);
		}
	}

	public static void waitUntilAllNetworkingUpdated(AmazonEC2 ec2, Collection<VirtualMachine> vms, long periodMillis) {
		// create copy so we can alter
		vms = new ArrayList<VirtualMachine>(vms);
		while (true) {
			Iterator<VirtualMachine> itr = vms.iterator();
			updateNetworking(ec2, vms);
			while (itr.hasNext()) {
				VirtualMachine vm = itr.next();
				String hn = vm.getHostname();
				if (hn != null && !hn.trim().equals("")) {
					itr.remove();
				}
			}
			if (vms.isEmpty()) {
				break;
			}
			JavaUtil.sleepAndLogInterruption(periodMillis);
		}
	}

	public static void updateNetworking(AmazonEC2 ec2, Collection<VirtualMachine> vms) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		Collection<String> instanceIds = vmsToInstanceIds(vms);
		try {
			describeInstancesRequest.setInstanceIds(instanceIds);
			DescribeInstancesResult results = ec2.describeInstances(describeInstancesRequest);
			for (Reservation r : results.getReservations()) {
				for (Instance i : r.getInstances()) {
					for (VirtualMachine vm : vms) {
						if (vm.getInfrastructureId().equals(i.getInstanceId())) {
							vm.setHostname(i.getPublicDnsName());
							vm.setIpAddress(i.getPublicIpAddress());
							vm.setInternalIpAddress(i.getPrivateIpAddress());
							vm.setInternalHostname(i.getPrivateDnsName());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to updated networking for VMs=" + vms + ".  Will retry.", e);
		}

	}

	public static List<String> vmsToInstanceIds(Collection<VirtualMachine> vms) {
		List<String> instanceIds = new ArrayList<String>(vms.size());
		for (VirtualMachine vm : vms) {
			instanceIds.add(vm.getInfrastructureId());
		}
		return instanceIds;
	}

	public static VirtualMachine provisionVm(AmazonEC2 ec2, VirtueUser user, VirtualMachineTemplate vmt,
			String namePrefix, InstanceType instanceType, Collection<String> securityGroups, String serverKeyName) {
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
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name,
				new ArrayList<ApplicationDefinition>(vmt.getApplications()), VmState.CREATING, vmt.getOs(),
				instance.getInstanceId(), instance.getPublicDnsName(), 22, loginUsername, null, privateKeyName,
				instance.getPublicIpAddress());
		return vm;
	}

	public static String getVpcIdFromSubnetId(String subnetId, AwsEc2Wrapper ec2Wrapper) {
		if (subnetId == null) {
			logger.error("Error no subnetId.  Cannot get VPC ID.");
			return null;
		}
		DescribeSubnetsRequest req = new DescribeSubnetsRequest();
		req.withSubnetIds(subnetId);
		DescribeSubnetsResult sub = ec2Wrapper.getEc2().describeSubnets(req);
		List<Subnet> subnets = sub.getSubnets();
		if (subnets.isEmpty()) {
			throw new IllegalArgumentException("Could not find subnet with id=" + subnetId);
		}
		return subnets.get(0).getVpcId();
	}

	public static String getSubnetIdFromName(String subnetName, AwsEc2Wrapper ec2Wrapper) {
		String newSubnetId = null;
		try {
			DescribeSubnetsRequest req = new DescribeSubnetsRequest();
			DescribeSubnetsResult sub = ec2Wrapper.getEc2().describeSubnets(req);

			for (Subnet subnet : sub.getSubnets()) {
				List<Tag> tags = subnet.getTags();
				for (Tag tag : tags) {
					if (tag.getKey().equalsIgnoreCase("name")) {
						if (tag.getValue().equalsIgnoreCase(subnetName)) {
							// match!
							if (newSubnetId == null) {
								newSubnetId = subnet.getSubnetId();
							} else {
								throw new SaviorException(SaviorException.UNKNOWN_ERROR,
										"Found multiple subnets with the name=" + subnetName);
							}
						}
					}
				}
			}
		} catch (SdkClientException e) {
			logger.error("Error with AWS.  Using null subnet", e);
		}
		return newSubnetId;
	}

	public static Collection<String> getSecurityGroupIdsByNameAndVpcId(Collection<String> defaultSecurityGroups,
			String vpcId, AwsEc2Wrapper ec2Wrapper) {

		ArrayList<String> securityGroupIds = new ArrayList<String>();
		try {
			DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest();
			DescribeSecurityGroupsResult sgs = ec2Wrapper.getEc2().describeSecurityGroups(req);
			for (SecurityGroup sg : sgs.getSecurityGroups()) {
				if (logger.isTraceEnabled()) {
					logger.trace("examining SG: id=" + sg.getGroupId() + " name=" + sg.getGroupName() + " vpcId="
							+ sg.getVpcId());
				}
				if (defaultSecurityGroups.contains(sg.getGroupName()) && sg.getVpcId().equals(vpcId)) {
					logger.trace("match!");
					securityGroupIds.add(sg.getGroupId());
				}
			}
			if (defaultSecurityGroups.size() != securityGroupIds.size()) {
				logger.warn("Found mismatch of security group name to ids.  #Names=" + defaultSecurityGroups.size()
						+ " #Ids=" + securityGroupIds.size() + " Names=" + defaultSecurityGroups);
			}
		} catch (SdkClientException e) {
			logger.error("Error with AWS.  Using blank security groups", e);
		}
		return securityGroupIds;
	}

}
