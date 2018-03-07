/* 
*  AwsManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Dec 14, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;

/**
 * {@link ICloudManager} implementation that uses AWS EC2 and Cloud Formation to
 * create and delete Virtues.
 *
 */
public class AwsManager implements ICloudManager {
	private static final String VIRTUE_PROFILE = "virtue";
	private static final String PROPERTY_AWS_PROFILE = "aws.profile";
	private static final Logger logger = LoggerFactory.getLogger(AwsManager.class);
	private static final int SSH_PORT = 22;
	private AWSCredentialsProvider credentialsProvider;
	private String privateKey;

	private AmazonEC2 ec2;
	// private AmazonS3 s3;
	// private AmazonSimpleDB sdb;
	private AmazonCloudFormation stackbuilder;

	String baseStack_Name = "SaviorStack-";
	private long vmStatePollPeriodMillis = 4000;
	private long stackCreationStatePollPeriodMillis = 3000;
	private SshKeyInjector sshKeyInjector;

	AwsManager(File privatekeyfile) {
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sshKeyInjector = new SshKeyInjector();
		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);

	}

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed automatically.
	 * Client parameters, such as proxies, can be specified in an optional
	 * ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.PropertiesCredentials
	 * @see com.amazonaws.ClientConfiguration
	 */
	private void init() throws AmazonClientException {

		/*
		 * The ProfileCredentialsProvider will return your [virtue] credential profile
		 * by reading from the credentials file located at (~/.aws/credentials).
		 */
		// AWSCredentialsProvider credentialsProvider = new
		// ProfileCredentialsProvider(VIRTUE);

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
		// s3 =
		// AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
		stackbuilder = AmazonCloudFormationClientBuilder.standard().withCredentials(credentialsProvider)
				.withRegion(Regions.US_EAST_1).build();
		/*
		 * sdb = AmazonSimpleDBClientBuilder.standard()
		 * .withCredentials(credentialsProvider) .withRegion("us-east-1") .build();
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ncc.savior.virtueadmin.infrastructure.ICloudManager#deleteVirtue(com.ncc.
	 * savior.virtueadmin.model.VirtueInstance)
	 */
	@Override
	public void deleteVirtue(VirtueInstance virtueInstance) {
		logger.debug("delete is being called, but not doing anything");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ncc.savior.virtueadmin.infrastructure.ICloudManager#createVirtue(com.ncc.
	 * savior.virtueadmin.model.User,
	 * com.ncc.savior.virtueadmin.model.VirtueTemplate)
	 */
	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {

		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/<user>/.aws/credentials), and is in valid format.", e);
		}

		VirtueInstance vi = null;
		String clientUser = user.getUsername();
		String serverUser = System.getProperty("user.name");
		String sshLoginUsername = "admin";
		String uuid = UUID.randomUUID().toString();
		String stackName = getStackName(clientUser, serverUser, uuid);
		// String logicalResourceName = "SampleNotificationTopic";

		String myCloudFormationFromFile = convertStreamToString(
				AwsManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));

		try {
			printTraceRunningInstances();
			// Create a stack
			CreateStackRequest createRequest = new CreateStackRequest();
			createRequest.setStackName(stackName);
			createRequest.setTemplateBody(myCloudFormationFromFile);

			logger.trace("Creating a stack called " + createRequest.getStackName() + ".");
			stackbuilder.createStack(createRequest);

			// Wait for stack to be created
			// Note that you could use SNS notifications on the CreateStack call to track
			// the progress of the stack creation
			String wait = waitForCompletion(stackbuilder, stackName);
			logger.trace("Stack creation completed, the stack " + stackName + " completed with " + wait);

			// Show all the stacks for this account along with the resources for each stack
			List<StackResource> createdEc2Instances = new ArrayList<StackResource>();

			for (Stack stack : stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()) {
				logger.trace("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

				if (stackName.equals(stack.getStackName())) {
					DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest();
					stackResourceRequest.setStackName(stack.getStackName());
					for (StackResource resource : stackbuilder.describeStackResources(stackResourceRequest)
							.getStackResources()) {

						/*
						 * Let's save the ec2 instance resource - we will use it later for getting more
						 * information about the instance such as dns, ip address etc.
						 */
						if (resource.getResourceType().equals("AWS::EC2::Instance")) {
							createdEc2Instances.add(resource);
							logger.trace(String.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(),
									resource.getLogicalResourceId(), resource.getPhysicalResourceId()));
						}

					}
				}
			}

			// EC2 Querying....
			Set<Instance> allInstances = getAllInstances();

			printTraceRunningInstances();
			Collection<VirtualMachine> vms = getNewInstances(template, sshLoginUsername, createdEc2Instances,
					allInstances);

			// waitForReachability(createdEc2Instances);
			renameAllVms(vms, "VRTU-" + clientUser + "-" + serverUser);
			AwsUtil.waitForAllVmsRunning(ec2, vms, vmStatePollPeriodMillis);

			addRsaKeyToVms(vms);
			// rebootAllVms(vms);

			vi = new VirtueInstance(template, clientUser, vms);
			vi.setId(uuid);

		} catch (AmazonServiceException ase) {
			if (ase.getErrorCode().equals("AlreadyExistsException")) {
				logger.error("Stack already exist", ase);
			} else {
				logger.error(
						"Caught an AmazonServiceException, which means your request made it "
								+ "to AWS CloudFormation, but was rejected with an error response for some reason.",
						ase);
				logger.error("  Error Message:    " + ase.getMessage());
				logger.error("  HTTP Status Code: " + ase.getStatusCode());
				logger.error("  AWS Error Code:   " + ase.getErrorCode());
				logger.error("  Error Type:       " + ase.getErrorType());
				logger.error("  Request ID:       " + ase.getRequestId());
			}
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "Error Creating stack", ase);

		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS CloudFormation, "
					+ "such as not being able to access the network.", ace);
		}

		// List<VirtualMachineTemplate> vmTemplates = template.getVmTemplates();
		/*
		 * Map<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();
		 * 
		 * VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(),
		 * template.getName(), template.getApplications(), VmState.RUNNING, OS.LINUX,
		 * UUID.randomUUID().toString(), "", 22); vms.put(vm.getId(), vm);
		 * 
		 * 
		 * 
		 */

		return vi;
	}

	private void addRsaKeyToVms(Collection<VirtualMachine> vms) {
		for (VirtualMachine vm : vms) {
			if (OS.LINUX.equals(vm.getOs())) {
				String newPrivateKey = null;
				try {
					newPrivateKey = sshKeyInjector.injectSshKey(vm);
				} catch (IOException | RuntimeException e) {
					logger.error("Injecting new SSH key failed.  Clients will not be able to login.", e);
				} finally {
					vm.setPrivateKey(newPrivateKey);
				}
			}
		}
	}

	private void renameAllVms(Collection<VirtualMachine> vms, String prefix) {
		for (VirtualMachine vm : vms) {
			CreateTagsRequest ctr = new CreateTagsRequest();
			ctr.withResources(vm.getInfrastructureId());
			Collection<Tag> tags = new ArrayList<Tag>();
			tags.add(new Tag("Name", prefix + "-" + vm.getName()));
			ctr.setTags(tags);
			ec2.createTags(ctr);
		}

	}

	protected void rebootAllVms(Collection<VirtualMachine> vms) {
		Map<String, VirtualMachine> instanceIdsToVm = new HashMap<String, VirtualMachine>();
		for (VirtualMachine vm : vms) {
			instanceIdsToVm.put(vm.getInfrastructureId(), vm);
		}
		RebootInstancesRequest reboot = new RebootInstancesRequest();
		reboot.setInstanceIds(instanceIdsToVm.keySet());
		ec2.rebootInstances(reboot);
		AwsUtil.updateStatusOnVms(ec2, vms);

		AwsUtil.waitForAllVmsRunning(ec2, vms, vmStatePollPeriodMillis);
	}

	private void printTraceRunningInstances() {
		if (logger.isTraceEnabled()) {
			Set<Instance> is = getAllInstances();
			logger.trace("You have " + is.size() + " Amazon EC2 instance(s) running.");
			logger.trace("Current running instances: ");
			int x = 0;
			for (Instance i : is) {
				x++;
				if (i.getState().getCode() < 33) {
					logger.trace("  " + x + ": " + i.getInstanceId() + " - " + i.getState().getName());
				}
			}
		}
	}

	private String getStackName(String clientUser, String serverUser, String uuid) {
		String stackName = baseStack_Name + clientUser + "-" + serverUser + "-" + uuid;
		return stackName;
	}

	private Collection<VirtualMachine> getNewInstances(VirtueTemplate template, String sshLoginUsername,
			List<StackResource> ec2InstancesResourcesCreated, Set<Instance> instances) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		// This only works for single VM virtues
		for (Instance ec2Instance : instances) {
			if (logger.isTraceEnabled() && ec2Instance.getState().getCode() < 33) {
				logger.trace("After ID:" + ec2Instance.getInstanceId() + " - " + ec2Instance.getState().getName());
			}
			for (StackResource sr : ec2InstancesResourcesCreated) {
				// String ec2InstanceId = ec2Instance.getInstanceId();
				// String myResourceID = sr.getPhysicalResourceId();

				if (ec2Instance.getInstanceId().equals(sr.getPhysicalResourceId())) {
					logger.trace("Found instance with id=" + ec2Instance);

					VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), template.getName(),
							template.getApplications(), VmState.LAUNCHING, OS.LINUX, ec2Instance.getInstanceId(),
							ec2Instance.getPublicDnsName(), SSH_PORT, sshLoginUsername, privateKey,
							ec2Instance.getPublicIpAddress());
					vms.add(vm);
				}
			}
		}
		return vms;
	}

	private Set<Instance> getAllInstances() {
		DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesRequest.getReservations();
		Set<Instance> instances = new HashSet<Instance>();

		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}
		return instances;
	}

	public String deleteVirtue(VirtueUser user, String instanceId) throws Exception {

		// Delete the stack

		DeleteStackRequest deleteRequest = new DeleteStackRequest();
		String userStackName = baseStack_Name + user.getUsername();
		deleteRequest.setStackName(userStackName);
		logger.trace("Deleting the stack called " + deleteRequest.getStackName() + ".");
		stackbuilder.deleteStack(deleteRequest);
		String stackName = null;// TODO need to find stack appropriately
		// Wait for stack to be deleted
		// Note that you could used SNS notifications on the original CreateStack call
		// to track the progress of the stack deletion
		try {
			String status = waitForCompletion(stackbuilder, stackName);
			logger.trace("Stack creation completed, the stack " + stackName + " completed with " + status);

		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS CloudFormation, but was rejected with an error response for some reason.", ase);
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS CloudFormation, "
					+ "such as not being able to access the network.", ace);
		}

		return instanceId;
	}

	// Convert a stream into a single, newline separated string
	public static String convertStreamToString(InputStream in) throws Exception {

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder stringbuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringbuilder.append(line + "\n");
		}
		in.close();
		return stringbuilder.toString();
	}

	// Wait for a stack to complete transitioning
	// End stack states are:
	// CREATE_COMPLETE
	// CREATE_FAILED
	// DELETE_FAILED
	// ROLLBACK_FAILED
	// OR the stack no longer exists
	public String waitForCompletion(AmazonCloudFormation stackbuilder, String stackName) throws Exception {

		DescribeStacksRequest wait = new DescribeStacksRequest();
		wait.setStackName(stackName);
		Boolean completed = false;
		String stackStatus = "Unknown";
		String stackReason = "";

		while (!completed) {
			List<Stack> stacks = stackbuilder.describeStacks(wait).getStacks();
			if (stacks.isEmpty()) {
				completed = true;
				stackStatus = "NO_SUCH_STACK";
				stackReason = "Stack has been deleted";
			} else {
				for (Stack stack : stacks) {
					if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString())
							|| stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString())
							|| stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString())
							|| stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
						completed = true;
						stackStatus = stack.getStackStatus();
						stackReason = stack.getStackStatusReason();
					}
				}
			}

			// Not done yet so sleep for seconds.
			if (!completed)
				Thread.sleep(stackCreationStatePollPeriodMillis);
		}

		// Show we are done
		logger.trace("Stack creation completed.  Stack=" + stackName);

		return stackStatus + (stackReason == null ? "" : " (" + stackReason + ")");
	}
}
