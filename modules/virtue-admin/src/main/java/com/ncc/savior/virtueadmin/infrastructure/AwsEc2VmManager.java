package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

public class AwsEc2VmManager implements IVmManager {
	private static final String PROPERTY_AWS_PROFILE = "aws.profile";
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = "VRTU-";
	private AWSCredentialsProvider credentialsProvider;
	private String privateKey;
	private AmazonEC2 ec2;
	private SshKeyInjector sshKeyInjector;
	private String serverKeyName;
	private ArrayList<String> defaultSecurityGroups;
	private String serverUser;
	private String awsProfile;
	private String defaultAmi;
	private String defaultLoginUsername;

	public AwsEc2VmManager(File privatekeyfile) {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.defaultSecurityGroups = new ArrayList<String>();
		// TODO much of this should be configurable instead of hard coded!
		this.defaultSecurityGroups.add("ssh_default_vpc");
		this.defaultSecurityGroups.add("default");
		this.sshKeyInjector = new SshKeyInjector();
		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);
		this.serverUser = System.getProperty("user.name");
		this.defaultLoginUsername = "admin";
	}

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
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
	}

	@Override
	public void addStateUpdateListener(IStateUpdateListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeStateUpdateListener(IStateUpdateListener listener) {
		// TODO Auto-generated method stub

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
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm) {
		List<String> instanceIds = new ArrayList<String>(1);
		instanceIds.add(vm.getInfrastructureId());
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
		ec2.terminateInstances(terminateInstancesRequest);
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

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates) {
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmTemplates.size());
		for (VirtualMachineTemplate vmt : vmTemplates) {
			RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

			// TODO template path should be the ami.
			String templatePath = vmt.getTemplatePath();
			templatePath = defaultAmi;
			runInstancesRequest = runInstancesRequest.withImageId(templatePath).withInstanceType(InstanceType.T2Small)
					.withMinCount(1).withMaxCount(1).withKeyName(serverKeyName)
					.withSecurityGroups(defaultSecurityGroups);
			RunInstancesResult result = ec2.runInstances(runInstancesRequest);

			List<Instance> instances = result.getReservation().getInstances();
			if (instances.size() != 1) {
				throw new RuntimeException("Created more than 1 instance!");
			}
			Instance instance = instances.get(0);
			String clientUser = user.getUsername();
			String name = VM_PREFIX + clientUser + "-" + serverUser + "-" + instance.getInstanceId();
			String loginUsername = defaultLoginUsername;
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
					VmState.CREATING, vmt.getOs(), instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT,
					loginUsername, privateKey, instance.getPublicIpAddress());
			vms.add(vm);
		}

		modifyVms(vms);
		return vms;
	}

	private void modifyVms(ArrayList<VirtualMachine> vms) {
		long a = System.currentTimeMillis();
		AwsUtil.waitUntilAllNetworkingUpdated(ec2, vms);
		for (VirtualMachine vm : vms) {
			CreateTagsRequest ctr = new CreateTagsRequest();
			ctr.withResources(vm.getInfrastructureId());
			Collection<Tag> tags = new ArrayList<Tag>();
			tags.add(new Tag("Name", vm.getName()));
			ctr.setTags(tags);
			ec2.createTags(ctr);
		}
		SshUtil.waitForAllVmsReachableParallel(vms, 2500);
		long b = System.currentTimeMillis();
		logger.debug("Vm's reachable after " + (b - a) / 1000.0 + " seconds");
		// AwsUtil.waitForAllVmsRunning(ec2, vms, 2500);
		// long c = System.currentTimeMillis();
		// logger.debug(
		// "Vm's aws-ready after " + (c - a) / 1000.0 + " seconds. (additional " + (c -
		// b) / 1000.0 + " seconds)");
		addRsaKeyToVms(vms, 3);
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
		} catch (AmazonEC2Exception e) {
			logger.warn("Warning terminating instances", e);
		}
	}

	private void addRsaKeyToVms(Collection<VirtualMachine> vms, int numberOfAttempts) {
		for (VirtualMachine vm : vms) {
			if (OS.LINUX.equals(vm.getOs())) {
				String newPrivateKey = null;
				// do while so we always attempt at least once
				do {
					try {
						numberOfAttempts--;
						newPrivateKey = sshKeyInjector.injectSshKey(vm);
						break;
					} catch (Exception e) {
						logger.error("Injecting new SSH key failed.  Clients will not be able to login.", e);
					} finally {

					}
				} while (numberOfAttempts > 0);
				vm.setPrivateKey(newPrivateKey);
			}
		}
	}

	public String getServerKeyName() {
		return serverKeyName;
	}

	public void setServerKeyName(String serverKeyName) {
		this.serverKeyName = serverKeyName;
	}

	public ArrayList<String> getDefaultSecurityGroups() {
		return defaultSecurityGroups;
	}

	public void setDefaultSecurityGroups(ArrayList<String> defaultSecurityGroups) {
		this.defaultSecurityGroups = defaultSecurityGroups;
	}

	public String getAwsProfile() {
		return awsProfile;
	}

	public void setAwsProfile(String awsProfile) {
		this.awsProfile = awsProfile;
	}

	public String getDefaultAmi() {
		return defaultAmi;
	}

	public void setDefaultAmi(String defaultAmi) {
		this.defaultAmi = defaultAmi;
	}
}
