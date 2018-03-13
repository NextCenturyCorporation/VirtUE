package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.ncc.savior.virtueadmin.infrastructure.BaseVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsVmUpdater.IUpdateNotifier;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;

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
	private static final String PROPERTY_AWS_PROFILE = "aws.profile";
	private static final Logger logger = LoggerFactory.getLogger(AsyncAwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = "VRTU-";
	private AWSCredentialsProvider credentialsProvider;
	private AmazonEC2 ec2;
	private String serverKeyName;
	private List<String> defaultSecurityGroups;
	private String serverUser;
	private String awsProfile;
	private String region;
	private InstanceType instanceType;
	private AwsVmUpdater vmUpdater;

	public AsyncAwsEc2VmManager(IKeyManager keyManager, String region, String awsProfile) {
		this.awsProfile = awsProfile;
		this.region = region;
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.defaultSecurityGroups = new ArrayList<String>();
		this.defaultSecurityGroups.add("default");

		this.serverUser = System.getProperty("user.name");
		this.instanceType = InstanceType.T2Small;

		this.vmUpdater = new AwsVmUpdater(ec2, new IUpdateNotifier() {
			@Override
			public void notifyUpdatedVms(Collection<VirtualMachine> vms) {
				notifyOnUpdateVms(vms);
			}

			@Override
			public void notifyUpdatedVm(VirtualMachine vm) {
				ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
				vms.add(vm);
				notifyOnUpdateVms(vms);
			}
		}, keyManager);

		this.addVmUpdateListener(new IVmUpdateListener() {

			@Override
			public void updateVmState(String vmId, VmState state) {
				logger.debug("updated state: vmid=" + vmId + " state=" + state);

			}

			@Override
			public void updateVms(Collection<VirtualMachine> vms) {
				logger.debug("updated VMs");
				for (VirtualMachine vm : vms) {
					logger.debug("  " + vm);
				}
			}
		});
	}

	/**
	 * initializes AWS/EC2 system mainly getting credentials.
	 * 
	 * @throws AmazonClientException
	 */
	private void init() throws AmazonClientException {
		// Set all AWS credential providers to use the virtue profile
		if (awsProfile != null && !awsProfile.trim().equals("")) {
			System.setProperty(PROPERTY_AWS_PROFILE, awsProfile);
		}
		// use the standard AWS credential provider chain so we can support a bunch of
		// different methods to get credentials.
		credentialsProvider = new AWSCredentialsProviderChain(new EnvironmentVariableCredentialsProvider(),
				new SystemPropertiesCredentialsProvider(), new ProfileCredentialsProvider(awsProfile),
				new PropertiesFileCredentialsProvider("./aws.properties"));
		try {
			credentialsProvider.getCredentials();

		} catch (Exception e) {
			logger.warn("Cannot load the credentials from the credential profiles file.  "
					+ "Use CLI to create credentials or add to ./aws.properties file.", e);
		}
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user, VirtualMachineTemplate vmt) {
		Collection<VirtualMachineTemplate> vmTemplates = new ArrayList<VirtualMachineTemplate>(1);
		Collection<VirtualMachine> vms = provisionVirtualMachineTemplates(user, vmTemplates);
		if (vms.size() != 1) {
			String msg = "Error provisioning VM.  Result has VM size of " + vms.size() + " and expected 1.";
			SaviorException e = new SaviorException(SaviorException.UNKNOWN_ERROR, msg);
			logger.error(msg, e);
			throw e;
		}
		return vms.iterator().next();
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates) {
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmTemplates.size());
		for (VirtualMachineTemplate vmt : vmTemplates) {
			RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

			String templatePath = vmt.getTemplatePath();
			runInstancesRequest = runInstancesRequest.withImageId(templatePath).withInstanceType(instanceType)
					.withMinCount(1).withMaxCount(1).withKeyName(serverKeyName)
					.withSecurityGroups(defaultSecurityGroups);
			RunInstancesResult result = ec2.runInstances(runInstancesRequest);

			List<Instance> instances = result.getReservation().getInstances();
			if (instances.size() != 1) {
				throw new RuntimeException("Created more than 1 instance when only 1 was expected!");
			}
			Instance instance = instances.get(0);
			String clientUser = user.getUsername();
			String name = VM_PREFIX + clientUser + "-" + serverUser + "-" + instance.getInstanceId();
			String loginUsername = vmt.getLoginUser();
			String privateKey = serverKeyName;
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
					VmState.CREATING, vmt.getOs(), instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT,
					loginUsername, privateKey, instance.getPublicIpAddress());
			vms.add(vm);
		}
		notifyOnUpdateVms(vms);
		vmUpdater.addVmToProvisionPipeline(vms);
		return vms;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		// TODO fix
		throw new NotImplementedException();
		// List<String> instanceIds = new ArrayList<String>(1);
		// instanceIds.add(vm.getInfrastructureId());
		// StartInstancesRequest startInstancesRequest = new
		// StartInstancesRequest(instanceIds);
		// ec2.startInstances(startInstancesRequest);
		// AwsUtil.updateStatusOnVm(ec2, vm);
		// notifyOnUpdateVmState(vm.getId(), vm.getState());
		// return vm;
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm) {
		// TODO fix
		throw new NotImplementedException();
		// List<String> instanceIds = new ArrayList<String>(1);
		// instanceIds.add(vm.getInfrastructureId());
		// StopInstancesRequest stopInstancesRequest = new
		// StopInstancesRequest(instanceIds);
		// ec2.stopInstances(stopInstancesRequest);
		// AwsUtil.updateStatusOnVm(ec2, vm);
		// notifyOnUpdateVmState(vm.getId(), vm.getState());
		// return vm;
	}

	@Override
	public Collection<VirtualMachine> startVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO fix
		throw new NotImplementedException();
		// List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		// StartInstancesRequest startInstancesRequest = new
		// StartInstancesRequest(instanceIds);
		// ec2.startInstances(startInstancesRequest);
		// AwsUtil.updateStatusOnVms(ec2, vms);
		// for (VirtualMachine vm : vms) {
		// notifyOnUpdateVmState(vm.getId(), vm.getState());
		// }
		// return vms;
	}

	@Override
	public Collection<VirtualMachine> stopVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO fix
		throw new NotImplementedException();
		// List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		// StopInstancesRequest stopInstancesRequest = new
		// StopInstancesRequest(instanceIds);
		// ec2.stopInstances(stopInstancesRequest);
		// AwsUtil.updateStatusOnVms(ec2, vms);
		// for (VirtualMachine vm : vms) {
		// notifyOnUpdateVmState(vm.getId(), vm.getState());
		// }
		// return vms;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm) {
		List<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		deleteVirtualMachines(vms);
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
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
			Collection<VirtualMachine> terminatedVms = new ArrayList<VirtualMachine>();
			for (InstanceStateChange r : result.getTerminatingInstances()) {
				String id = r.getInstanceId();
				for (VirtualMachine vm : vms) {
					if (vm.getInfrastructureId().equals(id)) {
						terminatedVms.add(vm);
						vm.setState(VmState.DELETING);
					}
				}
			}
			// TODO deleting to deleted?
			notifyOnUpdateVms(terminatedVms);
			// AwsVmUpdater.addVmsToDel
		} catch (AmazonEC2Exception e) {
			logger.warn("Error terminating instances", e);
		}
	}

	@Override
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

	public String getServerKeyName() {
		return serverKeyName;
	}

	public void setServerKeyName(String serverKeyName) {
		this.serverKeyName = serverKeyName;
	}

	public List<String> getDefaultSecurityGroups() {
		return defaultSecurityGroups;
	}

	public void setDefaultSecurityGroups(ArrayList<String> defaultSecurityGroups) {
		this.defaultSecurityGroups = defaultSecurityGroups;
	}

	public String getAwsProfile() {
		return awsProfile;
	}

	public void setDefaultSecurityGroupsCommaSeparated(String securityGroupsCommaSeparated) {
		String[] sgs = securityGroupsCommaSeparated.split(",");
		defaultSecurityGroups.clear();
		for (String sg : sgs) {
			defaultSecurityGroups.add(sg);
		}
	}

	public void setAwsProfile(String awsProfile) {
		this.awsProfile = awsProfile;
	}

	public String getServerUser() {
		return serverUser;
	}

	/**
	 * Sets the server user used for naming AWS VM's. If it is null or an empty
	 * string (after .trim()), the value will not be set. The default is set in the
	 * constructor with the java property "user.name"
	 * 
	 * @param serverUser
	 */
	public void setServerUser(String serverUser) {
		if (serverUser != null && !serverUser.trim().equals("")) {
			this.serverUser = serverUser;
		}
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

	public void setUpdateListener(IVmUpdateListener listener) {
		addVmUpdateListener(listener);
	}
}
