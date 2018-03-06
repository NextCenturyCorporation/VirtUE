package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;

public class AwsEc2VmManager implements IVmManager {
	private static final String VIRTUE_PROFILE = "virtue";
	private static final String PROPERTY_AWS_PROFILE = "aws.profile";
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = null;
	private AWSCredentialsProvider credentialsProvider;
	private String privateKey;
	private AmazonEC2 ec2;
	private SshKeyInjector sshKeyInjector;
	private String serverKeyName;
	private ArrayList<String> defaultSecurityGroups;
	private String serverUser;

	public AwsEc2VmManager(File privatekeyfile) {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.defaultSecurityGroups = new ArrayList<String>();
		this.defaultSecurityGroups.add("ssh_default_vpc");
		this.defaultSecurityGroups.add("default");
		this.sshKeyInjector = new SshKeyInjector();
		this.serverKeyName = "vrtu";
		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);
		this.serverUser = System.getProperty("user.name");
	}

	private void init() throws AmazonClientException {
		// Set all AWS credential providers to use the virtue profile
		System.setProperty(PROPERTY_AWS_PROFILE, VIRTUE_PROFILE);
		// use the standard AWS credential provider chain so we can support a bunch of
		// different methods to get credentials.
		credentialsProvider = new AWSCredentialsProviderChain(new EnvironmentVariableCredentialsProvider(),
				new SystemPropertiesCredentialsProvider(), new ProfileCredentialsProvider(VIRTUE_PROFILE),
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
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest = runInstancesRequest.withImageId(vmt.getTemplatePath())
				.withInstanceType(InstanceType.T2Small).withMinCount(1).withMaxCount(1).withKeyName(serverKeyName)
				.withSecurityGroups(defaultSecurityGroups);
		RunInstancesResult result = ec2.runInstances(runInstancesRequest);
		List<Instance> instances = result.getReservation().getInstances();
		if (instances.size() != 1) {
			throw new RuntimeException("Created more than 1 instance!");
		}
		Instance instance = instances.get(0);
		String clientUser = user.getUsername();
		String name = VM_PREFIX + clientUser + "-" + serverUser + "-" + instance.getInstanceId();
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
				VmState.CREATING,
				vmt.getOs(),
				instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT, clientUser, privateKey,
				instance.getPublicIpAddress());


		return vm;
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
		// TODO Auto-generated method stub

	}

	@Override
	public VmState getVirtialMachineState(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub

	}
}
