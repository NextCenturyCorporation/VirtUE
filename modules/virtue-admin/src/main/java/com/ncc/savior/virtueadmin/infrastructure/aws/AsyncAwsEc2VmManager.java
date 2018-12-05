package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.ncc.savior.virtueadmin.infrastructure.BaseVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

/**
 * {@link IVmManager} that uses AWS EC2 to create and manage VMs. The following
 * parameters are important for configuration:
 * <ul>
 * <li>region - set in constructor for the AWS region to be used.
 * <li>serverKeyName - the name of they key used for the VM's. They key will be
 * retrieved from the {@link IKeyManager}
 * <li>defaultSecurityGroups - a list of AWS security groups that will be
 * applies to all VMs
 * <li>serverUSer - The server user used for naming AWS VM's.
 * <li>awsProfile - the profile used to get AWS login credentials.
 * <li>instanceType - the AWS instance type that should be deployed I.E.
 * t2.small
 * </ul>
 *
 */
public class AsyncAwsEc2VmManager extends BaseVmManager {
	private static final Logger logger = LoggerFactory.getLogger(AsyncAwsEc2VmManager.class);
	private static final String VM_PREFIX = "VRTU-W-";
	private static final int TERMINATED = 48;
	private String serverKeyName;
	private List<String> defaultSecurityGroups;
	private String serverId;
	private String awsProfile;
	private InstanceType instanceType;
	private AwsEc2Wrapper ec2Wrapper;
	private Collection<String> securityGroupIds;
	private CompletableFutureServiceProvider serviceProvider;
	private IVpcSubnetProvider vpcSubnetProvider;

	/**
	 * 
	 * @param keyManager
	 *            - Handles storing keys.
	 * @param region
	 *            - AWS region
	 * @param awsProfile
	 *            - Profile used by AWS Credential Providers. It is particularly
	 *            passed to {@link ProfileCredentialsProvider}.
	 */
	public AsyncAwsEc2VmManager(CompletableFutureServiceProvider serviceProvider, IKeyManager keyManager,
			AwsEc2Wrapper ec2Wrapper, IVpcSubnetProvider vpcSubnetProvider, ServerIdProvider serverIdProvider) {
		this.ec2Wrapper = ec2Wrapper;
		this.defaultSecurityGroups = new ArrayList<String>();
		this.defaultSecurityGroups.add("default");

		this.instanceType = InstanceType.T2Small;
		this.serviceProvider = serviceProvider;
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.serverId = serverIdProvider.getServerId();
	}

	// @Override
	// public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user,
	// VirtualMachineTemplate vmt,
	// CompletableFuture<Collection<VirtualMachine>> future) {
	// Collection<VirtualMachineTemplate> vmTemplates = new
	// ArrayList<VirtualMachineTemplate>(1);
	// vmTemplates.add(vmt);
	//
	// Collection<VirtualMachine> vms = provisionVirtualMachineTemplates(user,
	// vmTemplates, future, null);
	// if (vms.size() != 1) {
	// String msg = "Error provisioning VM. Result has VM size of " + vms.size() + "
	// and expected 1.";
	// SaviorException e = new SaviorException(SaviorErrorCode.AWS_ERROR, msg);
	// logger.error(msg, e);
	// throw e;
	// }
	// return vms.iterator().next();
	// }

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates, CompletableFuture<Collection<VirtualMachine>> vmFutures,
			VirtueCreationAdditionalParameters virtueMods) {
		if (vmFutures == null) {
			vmFutures = new CompletableFuture<Collection<VirtualMachine>>();
		}
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmTemplates.size());
		for (VirtualMachineTemplate vmt : vmTemplates) {
			String clientUser = user.getUsername();
			String virtueName = virtueMods.getName() == null ? "" : "-" + virtueMods.getName();
			virtueName = virtueName.replace(" ", "-");
			String namePrefix = VM_PREFIX + serverId + "-" + clientUser + virtueName;

			Collection<String> secGroupIds = new HashSet<String>(securityGroupIds);
			if (virtueMods.getSecurityGroupId() != null) {
				secGroupIds.add(virtueMods.getSecurityGroupId());
			}
			virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.USER_VIRTUE);
			virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.WINDOWS);
			virtueMods.setUsername(user.getUsername());
			VirtualMachine vm = ec2Wrapper.provisionVm(vmt, namePrefix, secGroupIds, serverKeyName, instanceType,
					virtueMods, null);
			vms.add(vm);
		}
		notifyOnUpdateVms(vms);
		addVmToProvisionPipeline(vms, vmFutures);
		return vms;
	}

	private void addVmToProvisionPipeline(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> vmFutures) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(vm, v);
			cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
			cf = serviceProvider.getEnsureDeleteVolumeOnTermination().chainFutures(cf, v);
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
			cf = serviceProvider.getAddRsa().chainFutures(cf, v);
			// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(vmFutures);

	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm,
			CompletableFuture<Collection<VirtualMachine>> vmFuture) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms.add(vm);
		vms = startVirtualMachines(vms, vmFuture);
		return vms.iterator().next();
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm,
			CompletableFuture<Collection<VirtualMachine>> vmFuture) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms.add(vm);
		vms = stopVirtualMachines(vms, vmFuture);
		return vms.iterator().next();
	}

	@Override
	public Collection<VirtualMachine> startVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> vmFuture) {
		if (vmFuture == null) {
			vmFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (vms.isEmpty()) {
			vmFuture.complete(vms);
		} else {
			ec2Wrapper.startVirtualMachines(vms);
			notifyOnUpdateVms(vms);
			addVmsToStartingPipeline(vms, vmFuture);
		}
		return vms;
	}

	private void addVmsToStartingPipeline(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsNetworkingUpdateService().startFutures(vm, v);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(future);
	}

	@Override
	public Collection<VirtualMachine> stopVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> vmFuture) {
		if (vmFuture == null) {
			vmFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (vms.isEmpty()) {
			vmFuture.complete(vms);
		} else {
			ec2Wrapper.stopVirtualMachines(vms);
			notifyOnUpdateVms(vms);
			addVmsToStoppingPipeline(vms, vmFuture);
		}
		return vms;
	}

	private void addVmsToStoppingPipeline(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(vm, false);

			cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.STOPPED);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(future);
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> future) {
		List<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		deleteVirtualMachines(vms, future);
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		if (future == null) {
			future = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (!vms.isEmpty()) {
			Collection<VirtualMachine> terminatedVms = ec2Wrapper.deleteVirtualMachines(vms);
			// TODO deleting to deleted?
			notifyOnUpdateVms(terminatedVms);
			addVmsToDeletingPipeline(vms, future);
		} else {
			future.complete(vms);
		}

	}

	private void addVmsToDeletingPipeline(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> future) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(vm, false);
			cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.DELETED);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(future);
	}

	@Override
	public VmState getVirtualMachineState(VirtualMachine vm) {
		return ec2Wrapper.getVirtualMachineState(vm);
	}

	public String getServerKeyName() {
		return serverKeyName;
	}

	public void setServerKeyName(String serverKeyName) {
		this.serverKeyName = serverKeyName;
	}

	public List<String> getDefaultSecurityGroups() {
		return defaultSecurityGroups;
	}

	/**
	 * must be called after set {@link #setDefaultSubnetName(String)}
	 * 
	 * @param defaultSecurityGroups
	 */
	public void setDefaultSecurityGroups(List<String> defaultSecurityGroups) {
		this.defaultSecurityGroups = defaultSecurityGroups;
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(defaultSecurityGroups,
				vpcSubnetProvider.getVpcId(), ec2Wrapper);
	}

	public void setDefaultSecurityGroupsCommaSeparated(String securityGroupsCommaSeparated) {
		String[] sgs = securityGroupsCommaSeparated.split(",");
		List<String> secGroups = new ArrayList<String>();
		for (String sg : sgs) {
			secGroups.add(sg);
		}
		setDefaultSecurityGroups(secGroups);
	}

	public String getAwsProfile() {
		return awsProfile;
	}

	public void setAwsProfile(String awsProfile) {
		this.awsProfile = awsProfile;
	}

	public String getServerId() {
		return serverId;
	}

	public InstanceType getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceTypeString) {
		try {
			InstanceType instanceType = InstanceType.fromValue(instanceTypeString);
			if (instanceType != null) {
				this.instanceType = instanceType;
			}
		} catch (Exception e) {
			logger.error("Error attempting to sent instance type.", e);
		}
	}

	public void setUpdateListener(IUpdateListener<VirtualMachine> listener) {
		addVmUpdateListener(listener);
	}

	public void rebootVm(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> vmFuture) {
		if (vmFuture == null) {
			vmFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}

		CompletableFuture<Collection<VirtualMachine>> vmFutureFinal = vmFuture;
		CompletableFuture<Collection<VirtualMachine>> stopFuture = new CompletableFuture<Collection<VirtualMachine>>();
		stopVirtualMachine(vm, stopFuture);
		stopFuture.thenAccept((Collection<VirtualMachine> stoppedVm) -> {
			startVirtualMachine(vm, vmFutureFinal);
		});
	}

	public void syncAll(List<String> ids) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		Collection<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
		filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_PRIMARY)
				.withValues(AwsUtil.VirtuePrimaryPurpose.USER_VIRTUE.toString()));
		describeInstancesRequest.setFilters(filters);
		DescribeInstancesResult result = ec2Wrapper.getEc2().describeInstances(describeInstancesRequest);

		List<String> instancesToBeTerminated = new ArrayList<String>();
		for (Reservation res : result.getReservations()) {
			for (Instance instance : res.getInstances()) {
				List<Tag> tags = instance.getTags();
				String instanceId = AwsUtil.tagGet(tags, AwsUtil.TAG_VIRTUE_INSTANCE_ID);
				Integer stateCode = instance.getState().getCode();
				if (instanceId != null && !ids.contains(instanceId) && stateCode != TERMINATED) {
					instancesToBeTerminated.add(instance.getInstanceId());
				}
			}
		}
		if (!instancesToBeTerminated.isEmpty()) {
			try {
				logger.debug("Attempting to delete extra ec2 instances with ids=" + instancesToBeTerminated);
				TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(
						instancesToBeTerminated);
				ec2Wrapper.getEc2().terminateInstances(terminateInstancesRequest);
			} catch (AmazonEC2Exception e) {
				logger.warn("Failed to terminate extra ec2 instances.  InstanceIds=" + instancesToBeTerminated, e);
			}
		}
	}
}
