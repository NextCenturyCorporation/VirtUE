package com.ncc.savior.virtueadmin.infrastructure.aws;

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
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.ncc.savior.virtueadmin.infrastructure.BaseVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

import net.bytebuddy.agent.VirtualMachine;
import persistance.JpaVirtualMachine;
import persistance.JpaVirtualMachineTemplate;
import persistance.JpaVirtueUser;

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
public class AwsEc2VmManager extends BaseVmManager {
	private static final String PROPERTY_AWS_PROFILE = "aws.profile";
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = "VRTU-";
	private AWSCredentialsProvider credentialsProvider;
	private AmazonEC2 ec2;
	private SshKeyInjector sshKeyInjector;
	private String serverKeyName;
	private List<String> defaultSecurityGroups;
	private String serverUser;
	private String awsProfile;
	private IKeyManager keyManager;
	private String region;
	private InstanceType instanceType;

	public AwsEc2VmManager(IKeyManager keyManager, String region) {
		this.region = region;
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.defaultSecurityGroups = new ArrayList<String>();
		this.defaultSecurityGroups.add("default");
		this.sshKeyInjector = new SshKeyInjector();
		this.serverUser = System.getProperty("user.name");
		this.keyManager = keyManager;
		this.instanceType = InstanceType.T2Small;
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
	public JpaVirtualMachine provisionVirtualMachineTemplate(JpaVirtueUser user, JpaVirtualMachineTemplate vmt) {
		Collection<JpaVirtualMachineTemplate> vmTemplates = new ArrayList<JpaVirtualMachineTemplate>(1);
		Collection<JpaVirtualMachine> vms = provisionVirtualMachineTemplates(user, vmTemplates);
		if (vms.size() != 1) {
			String msg = "Error provisioning VM.  Result has VM size of " + vms.size() + " and expected 1.";
			SaviorException e = new SaviorException(SaviorException.UNKNOWN_ERROR, msg);
			logger.error(msg, e);
			throw e;
		}
		return vms.iterator().next();
	}

	@Override
	public JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm) {
		List<String> instanceIds = new ArrayList<String>(1);
		instanceIds.add(vm.getInfrastructureId());
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);
		ec2.startInstances(startInstancesRequest);
		AwsUtil.updateStatusOnVm(ec2, vm);
		notifyOnUpdateVmState(vm.getId(), vm.getState());
		return vm;
	}

	@Override
	public JpaVirtualMachine stopVirtualMachine(JpaVirtualMachine vm) {
		List<String> instanceIds = new ArrayList<String>(1);
		instanceIds.add(vm.getInfrastructureId());
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);
		ec2.stopInstances(stopInstancesRequest);
		AwsUtil.updateStatusOnVm(ec2, vm);
		notifyOnUpdateVmState(vm.getId(), vm.getState());
		return vm;
	}

	@Override
	public Collection<JpaVirtualMachine> startVirtualMachines(Collection<JpaVirtualMachine> vms) {
		List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);
		ec2.startInstances(startInstancesRequest);
		AwsUtil.updateStatusOnVms(ec2, vms);
		for (JpaVirtualMachine vm : vms) {
			notifyOnUpdateVmState(vm.getId(), vm.getState());
		}
		return vms;
	}

	@Override
	public Collection<JpaVirtualMachine> stopVirtualMachines(Collection<JpaVirtualMachine> vms) {
		List<String> instanceIds = AwsUtil.vmsToInstanceIds(vms);
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);
		ec2.stopInstances(stopInstancesRequest);
		AwsUtil.updateStatusOnVms(ec2, vms);
		for (JpaVirtualMachine vm : vms) {
			notifyOnUpdateVmState(vm.getId(), vm.getState());
		}
		return vms;
	}

	@Override
	public void deleteVirtualMachine(JpaVirtualMachine vm) {
		List<String> instanceIds = new ArrayList<String>(1);
		instanceIds.add(vm.getInfrastructureId());
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
		ec2.terminateInstances(terminateInstancesRequest);
	}

	@Override
	public VmState getVirtualMachineState(JpaVirtualMachine vm) {
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
	public Collection<JpaVirtualMachine> provisionVirtualMachineTemplates(JpaVirtueUser user,
			Collection<JpaVirtualMachineTemplate> vmTemplates) {
		ArrayList<JpaVirtualMachine> vms = new ArrayList<JpaVirtualMachine>(vmTemplates.size());
		for (JpaVirtualMachineTemplate vmt : vmTemplates) {
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
			String keyName = instance.getKeyName();
			String privateKey = null;
			if (keyName != null) {
				String pk = keyManager.getKeyByName(keyName);
				if (pk != null) {
					privateKey = pk;
				}
			}
			JpaVirtualMachine vm = new JpaVirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
					VmState.CREATING, vmt.getOs(), instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT,
					loginUsername, privateKey, keyName, instance.getPublicIpAddress());
			vms.add(vm);
		}
		JavaUtil.sleepAndLogInterruption(2000);
		modifyVms(vms);
		return vms;
	}

	/**
	 * After VM's are created, there are still some functions that we need to do to
	 * ensure we have all the information we need. This function handles those
	 * methods. This includes:
	 * <ul>
	 * <li>Get IP address and hostname
	 * <li>Name the VM in AWS
	 * <li>Wait until the VM is actually reachable
	 * <li>Add a new unique RSA key to the VM
	 * </ul>
	 * 
	 * @param vms
	 */
	private void modifyVms(ArrayList<JpaVirtualMachine> vms) {
		long a = System.currentTimeMillis();
		AwsUtil.waitUntilAllNetworkingUpdated(ec2, vms, 500);
		nameVmsInAws(vms);
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

	/**
	 * Renames the VMs based on the {@link VirtualMachine#getName()} method. The
	 * name is set earlier in the provision process.
	 * 
	 * @param vms
	 */
	private void nameVmsInAws(ArrayList<JpaVirtualMachine> vms) {
		vms = new ArrayList<JpaVirtualMachine>(vms);
		int tries = 3;
		while (!vms.isEmpty() && tries > 0) {
			Iterator<JpaVirtualMachine> itr = vms.iterator();
			while (itr.hasNext()) {
				JpaVirtualMachine vm = itr.next();
				try {
					CreateTagsRequest ctr = new CreateTagsRequest();
					ctr.withResources(vm.getInfrastructureId());
					Collection<Tag> tags = new ArrayList<Tag>();
					tags.add(new Tag("Autogen-Virtue-VM", serverUser));
					tags.add(new Tag("Name", vm.getName()));
					ctr.setTags(tags);
					ec2.createTags(ctr);
					itr.remove();
				} catch (Exception e) {
					logger.warn("failed to rename AWS machine for VM='" + vm.getName() + "': " + e.getMessage());
				}
				JavaUtil.sleepAndLogInterruption(750);
			}
			tries--;
		}
	}

	@Override
	public void deleteVirtualMachines(Collection<JpaVirtualMachine> vms) {
		try {
			List<String> instanceIds = new ArrayList<String>(vms.size());
			for (JpaVirtualMachine vm : vms) {
				instanceIds.add(vm.getInfrastructureId());
			}
			TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
			TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
			if (logger.isTraceEnabled()) {
				logger.trace("Terminating: " + result.getTerminatingInstances());
			}
		} catch (AmazonEC2Exception e) {
			logger.warn("Error terminating instances", e);
		}
	}

	/**
	 * Adds unique RSA keys to VM's for SSH login. The public key is played in the
	 * .ssh/authorized_keys file and the private key is stored in the database. This
	 * allows anyone with access to hte private key the ability to login to this VM
	 * and only this VM.
	 * 
	 * @param vms
	 * @param numberOfAttempts
	 */
	private void addRsaKeyToVms(Collection<JpaVirtualMachine> vms, int numberOfAttempts) {
		for (JpaVirtualMachine vm : vms) {
			if (OS.LINUX.equals(vm.getOs())) {
				String newPrivateKey = null;
				// do while so we always attempt at least once
				do {
					try {
						numberOfAttempts--;
						newPrivateKey = sshKeyInjector.injectSshKey(vm, vm.getPrivateKey());
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
}
