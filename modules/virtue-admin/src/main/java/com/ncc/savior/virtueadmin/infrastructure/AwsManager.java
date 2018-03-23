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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
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
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceStatusSummary;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.LinuxVirtualMachine;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.WindowsVirtualMachine;
import com.ncc.savior.virtueadmin.util.SaviorException;

public class AwsManager implements ICloudManager {
	private static final Logger logger = LoggerFactory.getLogger(AwsManager.class);
	private static final int SSH_PORT = 22;
	private static final int RDP_PORT = 3389;
	AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("virtue");
	private String privateKey;

	private AmazonEC2 ec2;
	//private AmazonS3 s3;
	// private AmazonSimpleDB sdb;
	private AmazonCloudFormation stackbuilder;

	String baseStack_Name = "SaviorStack-";
	private long vmStatePollPeriodMillis = 4000;
	private long stackCreationStatePollPeriodMillis = 3000;

	AwsManager(File privatekeyfile) {
		// credentialsProvider =new BasicCredentialsProvider();
		// credentialsProvider.setCredentials(new Authscope, credentials);
		try {
			init();
		} catch (AmazonClientException e) {
			throw new SaviorException(SaviorException.ErrorCode.CLOUD_ERROR, "unknown error", e);
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
	private void init() throws AmazonClientException {

		/*
		 * The ProfileCredentialsProvider will return your [virtue] credential profile
		 * by reading from the credentials file located at (~/.aws/credentials).
		 */
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("virtue");

		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			logger.warn("Cannot load the credentials from the credential profiles file. ", e);
			try {
				credentialsProvider = new PropertiesFileCredentialsProvider("aws.properties");
			} catch (Exception e2) {
				logger.warn("Cannot load credentials from credentials file: aws.properties", e2);
				throw new AmazonEC2Exception("Cannot load credentials.  Use cli or aws.properties");
			}
		}
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
		//s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
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

		String linuxCloudFormationTemplate = convertStreamToString(
				AwsManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));
		String windowsCloudFormationTemplate = convertStreamToString(
				AwsManager.class.getResourceAsStream("/aws-templates/WindowsVM.template"));

		try {
			printTraceRunningInstances();
			// Create a stack
			CreateStackRequest createRequest = new CreateStackRequest();
			createRequest.setStackName(stackName);
			// TODO make this work for Virtues with > 1 VM
			OS os = template.getVmTemplates().iterator().next().getOs();
			String templateBody;
			switch (os) {
			case LINUX:
				templateBody = linuxCloudFormationTemplate;
				break;
			case WINDOWS:
				templateBody = windowsCloudFormationTemplate;
				break;
			default:
				throw new InternalError("unsupported OS type: " + os);
			}
			createRequest.setTemplateBody(templateBody);

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
			Collection<AbstractVirtualMachine> vms = getNewInstances(template, sshLoginUsername, createdEc2Instances,
					allInstances);

			// waitForReachability(createdEc2Instances);
			renameAllVms(vms, "VRTU-" + clientUser + "-" + serverUser);
			waitForAllVmsRunning(vms);

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
			throw new SaviorException(SaviorException.ErrorCode.UNKNOWN_ERROR, "Error Creating stack", ase);

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

	private void renameAllVms(Collection<AbstractVirtualMachine> vms, String prefix) {
		for (AbstractVirtualMachine vm : vms) {
			CreateTagsRequest ctr = new CreateTagsRequest();
			ctr.withResources(vm.getInfrastructureId());
			Collection<Tag> tags = new ArrayList<Tag>();
			tags.add(new Tag("Name", prefix + "-" + vm.getName()));
			ctr.setTags(tags);
			ec2.createTags(ctr);
		}

	}

	private void rebootAllVms(Collection<AbstractVirtualMachine> vms) {
		Map<String, AbstractVirtualMachine> instanceIdsToVm = new HashMap<String, AbstractVirtualMachine>();
		for (AbstractVirtualMachine vm : vms) {
			instanceIdsToVm.put(vm.getInfrastructureId(), vm);
		}
		RebootInstancesRequest reboot = new RebootInstancesRequest();
		reboot.setInstanceIds(instanceIdsToVm.keySet());
		ec2.rebootInstances(reboot);
		updateStatusOnVms(vms);

		waitForAllVmsRunning(vms);
	}

	private void printTraceRunningInstances() {
		if (logger.isTraceEnabled()) {
			Set<Instance> instances = getAllInstances();
			logger.trace("You have " + instances.size() + " Amazon EC2 instance(s) running.");
			logger.trace("Current running instances: ");
			int x = 0;
			for (Instance i : instances) {
				x++;
				if (i.getState().getCode() < 33) {
					logger.trace("  " + x + ": " + i.getInstanceId() + " - " + i.getState().getName());
				}
			}
		}
	}

	public Collection<AbstractVirtualMachine> updateStatusOnVms(Collection<AbstractVirtualMachine> vms) {
		Map<String, AbstractVirtualMachine> instanceIdsToVm = new HashMap<String, AbstractVirtualMachine>();
		for (AbstractVirtualMachine vm : vms) {
			instanceIdsToVm.put(vm.getInfrastructureId(), vm);
		}
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.setInstanceIds(instanceIdsToVm.keySet());
		DescribeInstanceStatusResult statusResult = ec2.describeInstanceStatus(describeInstanceStatusRequest);
		Iterator<InstanceStatus> itr = statusResult.getInstanceStatuses().iterator();
		while (itr.hasNext()) {
			InstanceStatus status = itr.next();
			AbstractVirtualMachine vm = instanceIdsToVm.get(status.getInstanceId());
			vm.setState(awsStatusToSaviorState(status));
		}
		return vms;

	}

	private VmState awsStatusToSaviorState(InstanceStatus status) {
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

	private VmState getRunningStateFromStatus(InstanceStatusSummary vmStatus, String instanceId) {
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

	private void waitForAllVmsRunning(Collection<AbstractVirtualMachine> vms) {
		boolean throwOnErrorState = true;

		while (!areAllVmsRunning(vms, throwOnErrorState)) {
			try {
				Thread.sleep(vmStatePollPeriodMillis);
			} catch (InterruptedException e) {
				logger.error("Poll sleep interrupted!");
			}
			vms = updateStatusOnVms(vms);
		}
	}

	private boolean areAllVmsRunning(Collection<AbstractVirtualMachine> vms, boolean throwOnErrorState) {
		logger.trace("Checking status of VMs:");
		for (AbstractVirtualMachine vm : vms) {
			logger.trace("  " + vm.toString());
			VmState state = vm.getState();
			if (VmState.RUNNING.equals(state)) {
				continue;
			} else if (throwOnErrorState && (state == null || VmState.ERROR.equals(state))) {
				throw new SaviorException(SaviorException.ErrorCode.VM_ERROR,
						"VM is not running. VM=" + vm + " state=" + state);
			} else {
				return false;
			}
		}
		return true;
	}

	private String getStackName(String clientUser, String serverUser, String uuid) {
		String stackName = baseStack_Name + clientUser + "-" + serverUser + "-" + uuid;
		return stackName;
	}

	private Collection<AbstractVirtualMachine> getNewInstances(VirtueTemplate template, String sshLoginUsername,
			List<StackResource> ec2InstancesResourcesCreated, Set<Instance> instances) {
		Collection<AbstractVirtualMachine> vms = new ArrayList<AbstractVirtualMachine>();
		// This only works for single VM virtues
		for (Instance ec2Instance : instances) {
			// code < 33 indicates pending, running, or shutting down 
			if (logger.isTraceEnabled() && (ec2Instance.getState().getCode() & 0xFF) < 33) {
				logger.trace("After ID:" + ec2Instance.getInstanceId() + " - " + ec2Instance.getState().getName());
			}
			for (StackResource sr : ec2InstancesResourcesCreated) {
				// String ec2InstanceId = ec2Instance.getInstanceId();
				// String myResourceID = sr.getPhysicalResourceId();

				if (ec2Instance.getInstanceId().equals(sr.getPhysicalResourceId())) {
					logger.trace("Found instance with id=" + ec2Instance);
					AbstractVirtualMachine vm;
					String vmId = UUID.randomUUID().toString();
					if ("windows".equalsIgnoreCase(ec2Instance.getPlatform())) {
						vm = new WindowsVirtualMachine(vmId, template.getName(), template.getApplications(),
								VmState.LAUNCHING, OS.WINDOWS, ec2Instance.getInstanceId(),
								ec2Instance.getPublicDnsName(), ec2Instance.getPublicIpAddress(), RDP_PORT);
					}
					else {
						vm = new LinuxVirtualMachine(vmId, template.getName(),
								template.getApplications(), VmState.LAUNCHING, OS.LINUX, ec2Instance.getInstanceId(),
								ec2Instance.getPublicDnsName(), SSH_PORT, sshLoginUsername, this.privateKey,
								ec2Instance.getPublicIpAddress());
					}
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
					String status = stack.getStackStatus();
					switch (StackStatus.fromValue(status)) {
					case CREATE_COMPLETE:
					case CREATE_FAILED:
					case ROLLBACK_COMPLETE:
					case ROLLBACK_FAILED:
					case DELETE_COMPLETE:
					case DELETE_FAILED:
						completed = true;
						stackStatus = status;
						stackReason = stack.getStackStatusReason();
						break;
					default:
						break;
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
