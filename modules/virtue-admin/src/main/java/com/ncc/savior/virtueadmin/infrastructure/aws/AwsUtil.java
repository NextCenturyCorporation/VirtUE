package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceStatusSummary;
import com.amazonaws.services.ec2.model.Reservation;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;
import com.ncc.savior.virtueadmin.util.SaviorException;

import persistance.JpaVirtualMachine;

/**
 * Utility functions for managing and updating AWS EC2 VMs.
 *
 */
public class AwsUtil {
	private static final Logger logger = LoggerFactory.getLogger(AwsUtil.class);

	public static Collection<JpaVirtualMachine> updateStatusOnVms(AmazonEC2 ec2, Collection<JpaVirtualMachine> vms) {
		Map<String, JpaVirtualMachine> instanceIdsToVm = new HashMap<String, JpaVirtualMachine>();
		for (JpaVirtualMachine vm : vms) {
			instanceIdsToVm.put(vm.getInfrastructureId(), vm);
		}
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.setInstanceIds(instanceIdsToVm.keySet());
		DescribeInstanceStatusResult statusResult = ec2.describeInstanceStatus(describeInstanceStatusRequest);
		Iterator<InstanceStatus> itr = statusResult.getInstanceStatuses().iterator();
		while (itr.hasNext()) {
			InstanceStatus status = itr.next();
			JpaVirtualMachine vm = instanceIdsToVm.get(status.getInstanceId());
			vm.setState(awsStatusToSaviorState(status));
		}
		return vms;
	}

	public static JpaVirtualMachine updateStatusOnVm(AmazonEC2 ec2, JpaVirtualMachine vm) {
		Map<String, JpaVirtualMachine> instanceIdsToVm = new HashMap<String, JpaVirtualMachine>();
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

	public static boolean areAllVmsRunning(Collection<JpaVirtualMachine> vms, boolean throwOnErrorState) {
		logger.trace("Checking status of VMs:");
		for (JpaVirtualMachine vm : vms) {
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

	public static void waitForAllVmsRunning(AmazonEC2 ec2, Collection<JpaVirtualMachine> vms,
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

	public static void waitUntilAllNetworkingUpdated(AmazonEC2 ec2, Collection<JpaVirtualMachine> vms,
			long periodMillis) {
		// create copy so we can alter
		vms = new ArrayList<JpaVirtualMachine>(vms);
		while (true) {
			Iterator<JpaVirtualMachine> itr = vms.iterator();
			updateNetworking(ec2, vms);
			while (itr.hasNext()) {
				JpaVirtualMachine vm = itr.next();
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

	public static void updateNetworking(AmazonEC2 ec2, Collection<JpaVirtualMachine> vms) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		Collection<String> instanceIds = vmsToInstanceIds(vms);
		try {
			describeInstancesRequest.setInstanceIds(instanceIds);
			DescribeInstancesResult results = ec2.describeInstances(describeInstancesRequest);
			for (Reservation r : results.getReservations()) {
				for (Instance i : r.getInstances()) {
					for (JpaVirtualMachine vm : vms) {
						if (vm.getInfrastructureId().equals(i.getInstanceId())) {
							vm.setHostname(i.getPublicDnsName());
							vm.setIpAddress(i.getPublicIpAddress());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to updated networking for VMs=" + vms + ".  Will retry.", e);
		}

	}

	public static List<String> vmsToInstanceIds(Collection<JpaVirtualMachine> vms) {
		List<String> instanceIds = new ArrayList<String>(vms.size());
		for (JpaVirtualMachine vm : vms) {
			instanceIds.add(vm.getInfrastructureId());
		}
		return instanceIds;
	}

}
