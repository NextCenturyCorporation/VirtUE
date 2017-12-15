/* 
*  AwsManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Dec 14, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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




public class AwsManager {
    ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("virtue");
	private StaticMachineVmManager vmManager;
	private IActiveVirtueDao virtueDao; 
    
    AwsManager(StaticMachineVmManager staticMachineVmManager, IActiveVirtueDao virtueDao)
    {
    		this.vmManager = staticMachineVmManager; 
    		this.virtueDao = virtueDao; 
    }

	public void deleteVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub
		
	}

	public VirtueInstance createVirtue(User user, VirtueTemplate template) throws Exception {
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/womitowoju/.aws/credentials), and is in valid format.", e);
		}

		AmazonCloudFormation stackbuilder = AmazonCloudFormationClientBuilder.standard()
				.withCredentials(credentialsProvider).withRegion(Regions.US_EAST_1).build();

		System.out.println("===========================================");
		System.out.println("Getting Started with AWS CloudFormation");
		System.out.println("===========================================\n");

		String stackName = "WoleStack-10";
		String logicalResourceName = "SampleNotificationTopic";

		String myCloudFormationFromFile = convertStreamToString(
				AwsManager.class.getResourceAsStream("/aws-templates/BrowserVirtue.template"));

		try {
			// Create a stack
			CreateStackRequest createRequest = new CreateStackRequest();
			createRequest.setStackName(stackName);
			createRequest.setTemplateBody(myCloudFormationFromFile);

			// createRequest.setTemplateBody(myCloudFormation);

			System.out.println("Creating a stack called " + createRequest.getStackName() + ".");
			stackbuilder.createStack(createRequest);

			// Wait for stack to be created
			// Note that you could use SNS notifications on the CreateStack call to track
			// the progress of the stack creation
			System.out.println("Stack creation completed, the stack " + stackName + " completed with "
					+ waitForCompletion(stackbuilder, stackName));

			// Show all the stacks for this account along with the resources for each stack
			for (Stack stack : stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()) {
				System.out.println("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

				DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest();
				stackResourceRequest.setStackName(stack.getStackName());
				for (StackResource resource : stackbuilder.describeStackResources(stackResourceRequest)
						.getStackResources()) {
					System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(),
							resource.getLogicalResourceId(), resource.getPhysicalResourceId());
				}
			}

			// Lookup a resource by its logical name
			DescribeStackResourcesRequest logicalNameResourceRequest = new DescribeStackResourcesRequest();
			logicalNameResourceRequest.setStackName(stackName);
			logicalNameResourceRequest.setLogicalResourceId(logicalResourceName);
			System.out.format("Looking up resource name %1$s from stack %2$s\n",
					logicalNameResourceRequest.getLogicalResourceId(), logicalNameResourceRequest.getStackName());
			for (StackResource resource : stackbuilder.describeStackResources(logicalNameResourceRequest)
					.getStackResources()) {
				System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(),
						resource.getLogicalResourceId(), resource.getPhysicalResourceId());
			}
			
	        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
	        //ec2.describeInsta


			/*
			// Delete the stack
			DeleteStackRequest deleteRequest = new DeleteStackRequest();
			deleteRequest.setStackName(stackName);
			System.out.println("Deleting the stack called " + deleteRequest.getStackName() + ".");
			stackbuilder.deleteStack(deleteRequest);

			// Wait for stack to be deleted
			// Note that you could used SNS notifications on the original CreateStack call
			// to track the progress of the stack deletion
			System.out.println("Stack creation completed, the stack " + stackName + " completed with "
					+ waitForCompletion(stackbuilder, stackName));
			*/

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
		
		//List<VirtualMachineTemplate> vmTemplates = template.getVmTemplates();
		/*
		Map<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();

		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), template.getName(), template.getApplications(),
					VmState.RUNNING, OS.LINUX, UUID.randomUUID().toString(), "", 22);
		vms.put(vm.getId(), vm);
		
	
		
		
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		virtueDao.addVirtue(vi);
		*/
		
		return null;

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
    //    CREATE_COMPLETE
    //    CREATE_FAILED
    //    DELETE_FAILED
    //    ROLLBACK_FAILED
    // OR the stack no longer exists
    public static String waitForCompletion(AmazonCloudFormation stackbuilder, String stackName) throws Exception {

        DescribeStacksRequest wait = new DescribeStacksRequest();
        wait.setStackName(stackName);
        Boolean completed = false;
        String  stackStatus = "Unknown";
        String  stackReason = "";

        System.out.print("Waiting");

        while (!completed) {
            List<Stack> stacks = stackbuilder.describeStacks(wait).getStacks();
            if (stacks.isEmpty())
            {
                completed   = true;
                stackStatus = "NO_SUCH_STACK";
                stackReason = "Stack has been deleted";
            } else {
                for (Stack stack : stacks) {
                    if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                            stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
                        completed = true;
                        stackStatus = stack.getStackStatus();
                        stackReason = stack.getStackStatusReason();
                    }
                }
            }

            // Show we are waiting
            System.out.print(".");

            // Not done yet so sleep for 10 seconds.
            if (!completed) Thread.sleep(10000);
        }

        // Show we are done
        System.out.print("done\n");

        return stackStatus + " (" + stackReason + ")";
    }

}
