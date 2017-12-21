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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;

public class AwsManager implements ICloudManager {
	private static final Logger logger = LoggerFactory.getLogger(AwsManager.class);
	private static final int SSH_PORT = 22;
	AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("virtue");
	private String privateKey;

	private AmazonEC2 ec2;
	private AmazonS3 s3;
	// private AmazonSimpleDB sdb;
	private AmazonCloudFormation stackbuilder;

	String baseStack_Name = "SaviorStack-";

	AwsManager(File privatekeyfile) {
		// credentialsProvider =new BasicCredentialsProvider();
		// credentialsProvider.setCredentials(new Authscope, credentials);
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	private void init() throws Exception {

		/*
		 * The ProfileCredentialsProvider will return your [virtue] credential profile
		 * by reading from the credentials file located at
		 * (/Users/womitowoju/.aws/credentials).
		 */
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("virtue");

		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/womitowoju/.aws/credentials), and is in valid format.", e);
		}
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
		s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
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
		// TODO Auto-generated method stub

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
	public VirtueInstance createVirtue(User user, VirtueTemplate template) throws Exception {

		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/womitowoju/.aws/credentials), and is in valid format.", e);
		}

		System.out.println("===========================================");
		System.out.println("Getting Started with AWS CloudFormation");
		System.out.println("===========================================\n");

		VirtueInstance vi = null;
		String clientUser = user.getUsername();
		String serverUser = System.getProperty("user.name");
		String sshLoginUsername = "admin";
		String uuid = UUID.randomUUID().toString();
		String stackName = getStackName(clientUser, serverUser, uuid);
		String logicalResourceName = "SampleNotificationTopic";

		String myCloudFormationFromFile = convertStreamToString(
				AwsManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));

		try {
			Set<Instance> is = getAllInstances();
			System.out.println("You have " + is.size() + " Amazon EC2 instance(s) running.");
			for (Instance i : is) {
				if (i.getState().getCode() < 33) {
					System.out.println("Prior ID:" + i.getInstanceId() + " - " + i.getState().getName());
				}
			}
			// Create a stack
			CreateStackRequest createRequest = new CreateStackRequest();
			createRequest.setStackName(stackName);
			createRequest.setTemplateBody(myCloudFormationFromFile);

			System.out.println("Creating a stack called " + createRequest.getStackName() + ".");
			CreateStackResult result = stackbuilder.createStack(createRequest);
			String stackId = result.getStackId();

			// Wait for stack to be created
			// Note that you could use SNS notifications on the CreateStack call to track
			// the progress of the stack creation
			String wait = waitForCompletion(stackbuilder, stackName);
			System.out.println("Stack creation completed, the stack " + stackName + " completed with " + wait);

			// Show all the stacks for this account along with the resources for each stack
			List<StackResource> ec2InstancesResourcesCreated = new ArrayList<StackResource>();

			for (Stack stack : stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()) {
				System.out.println("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

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
							ec2InstancesResourcesCreated.add(resource);
							System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(),
									resource.getLogicalResourceId(), resource.getPhysicalResourceId());
						}

					}
				}
			}

			/*
			 * // Lookup a resource by its logical name DescribeStackResourcesRequest
			 * logicalNameResourceRequest = new DescribeStackResourcesRequest();
			 * logicalNameResourceRequest.setStackName(stackName);
			 * logicalNameResourceRequest.setLogicalResourceId(logicalResourceName);
			 * System.out.format("Looking up resource name %1$s from stack %2$s\n",
			 * logicalNameResourceRequest.getLogicalResourceId(),
			 * logicalNameResourceRequest.getStackName()); for (StackResource resource :
			 * stackbuilder.describeStackResources(logicalNameResourceRequest)
			 * .getStackResources()) { System.out.format("    %1$-40s %2$-25s %3$s\n",
			 * resource.getResourceType(), resource.getLogicalResourceId(),
			 * resource.getPhysicalResourceId()); }
			 */

			// EC2 Querying....
			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
			List<Reservation> reservations = describeInstancesRequest.getReservations();
			Set<Instance> instances = new HashSet<Instance>();

			for (Reservation reservation : reservations) {
				instances.addAll(reservation.getInstances());
			}


			Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();

			System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");

			for (StackResource sr : ec2InstancesResourcesCreated) {
				System.out.println(sr.getLogicalResourceId() + " - " + sr.getPhysicalResourceId());
			}

			vms = getNewInstances(template, sshLoginUsername, ec2InstancesResourcesCreated, instances, vms);

			vi = new VirtueInstance(template, clientUser, vms);
			vi.setId(uuid);

		} catch (AmazonServiceException ase) {

			if (ase.getErrorCode().equals("AlreadyExistsException")) {
				System.out.println("Stack already exist");
				// EC2 Querying....
				DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
				List<Reservation> reservations = describeInstancesRequest.getReservations();
				Set<Instance> instances = new HashSet<Instance>();

				for (Reservation reservation : reservations) {
					instances.addAll(reservation.getInstances());
				}

				System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");

			} else {
				System.out.println("Caught an AmazonServiceException, which means your request made it "
						+ "to AWS CloudFormation, but was rejected with an error response for some reason.");
				System.out.println("Error Message:    " + ase.getMessage());
				System.out.println("HTTP Status Code: " + ase.getStatusCode());
				System.out.println("AWS Error Code:   " + ase.getErrorCode());
				System.out.println("Error Type:       " + ase.getErrorType());
				System.out.println("Request ID:       " + ase.getRequestId());
			}

		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS CloudFormation, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
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

	private String getStackName(String clientUser, String serverUser, String uuid) {
		String stackName = baseStack_Name + clientUser + "-" + serverUser + "-"
				+ uuid;
		return stackName;
	}

	private Collection<VirtualMachine> getNewInstances(VirtueTemplate template, String sshLoginUsername,
			List<StackResource> ec2InstancesResourcesCreated, Set<Instance> instances, Collection<VirtualMachine> vms) {
		// This only works for single VM virtues
		for (Instance ec2Instance : instances) {
			if (ec2Instance.getState().getCode() < 33) {
				System.out.println(
						"After ID:" + ec2Instance.getInstanceId() + " - " + ec2Instance.getState().getName());
			}
			for (StackResource sr : ec2InstancesResourcesCreated) {
				// String ec2InstanceId = ec2Instance.getInstanceId();
				// String myResourceID = sr.getPhysicalResourceId();

				if (ec2Instance.getInstanceId().equals(sr.getPhysicalResourceId())) {
					System.out.println("Found it!!!!!!!!!!!!!!");

					VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), template.getName(),
							template.getApplications(), VmState.RUNNING, OS.LINUX, UUID.randomUUID().toString(),
							ec2Instance.getPublicDnsName(), SSH_PORT, sshLoginUsername, this.privateKey,
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

	public String deleteVirtue(User user, String instanceId) throws Exception {

		// Delete the stack

		DeleteStackRequest deleteRequest = new DeleteStackRequest();
		String userStackName = baseStack_Name + user.getUsername();
		deleteRequest.setStackName(userStackName);
		System.out.println("Deleting the stack called " + deleteRequest.getStackName() + ".");
		stackbuilder.deleteStack(deleteRequest);
		String stackName = null;// TODO need to find stack appropriately
		// Wait for stack to be deleted
		// Note that you could used SNS notifications on the original CreateStack call
		// to track the progress of the stack deletion
		try {


			System.out.println("Stack creation completed, the stack " + stackName + " completed with "
					+ waitForCompletion(stackbuilder, stackName));

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS CloudFormation, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS CloudFormation, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
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
	public static String waitForCompletion(AmazonCloudFormation stackbuilder, String stackName) throws Exception {

		DescribeStacksRequest wait = new DescribeStacksRequest();
		wait.setStackName(stackName);
		Boolean completed = false;
		String stackStatus = "Unknown";
		String stackReason = "";

		System.out.print("Waiting");

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

			// Show we are waiting
			System.out.print(".");

			// Not done yet so sleep for 10 seconds.
			if (!completed)
				Thread.sleep(10000);
		}

		// Show we are done
		System.out.print("done\n");

		return stackStatus + " (" + stackReason + ")";
	}

}
