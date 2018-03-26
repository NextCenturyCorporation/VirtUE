package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.model.InstanceType;
import com.ncc.savior.virtueadmin.infrastructure.BaseVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmManager;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdater;
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
	private static final Logger logger = LoggerFactory.getLogger(AsyncAwsEc2VmManager.class);
	private static final String VM_PREFIX = "VRTU-";
	private String serverKeyName;
	private List<String> defaultSecurityGroups;
	private String serverUser;
	private String awsProfile;
	private InstanceType instanceType;
	private IVmUpdater vmUpdater;
	private AwsEc2Wrapper ec2Wrapper;

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
	public AsyncAwsEc2VmManager(IVmUpdater updater, IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper) {
		this.ec2Wrapper = ec2Wrapper;
		this.defaultSecurityGroups = new ArrayList<String>();
		this.defaultSecurityGroups.add("default");

		this.serverUser = System.getProperty("user.name");
		this.instanceType = InstanceType.T2Small;
		this.vmUpdater = updater;
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
			String clientUser = user.getUsername();
			String namePrefix = VM_PREFIX + clientUser + "-" + serverUser + "-";
			VirtualMachine vm = ec2Wrapper.provisionVm(vmt, namePrefix, defaultSecurityGroups, serverKeyName,
					instanceType);
			vms.add(vm);
		}
		notifyOnUpdateVms(vms);
		vmUpdater.addVmToProvisionPipeline(vms);
		return vms;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms = startVirtualMachines(vms);
		return vms.iterator().next();
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms = stopVirtualMachines(vms);
		return vms.iterator().next();
	}

	@Override
	public Collection<VirtualMachine> startVirtualMachines(Collection<VirtualMachine> vms) {
		ec2Wrapper.startVirtualMachines(vms);
		notifyOnUpdateVms(vms);
		vmUpdater.addVmsToStartingPipeline(vms);
		return vms;
	}

	@Override
	public Collection<VirtualMachine> stopVirtualMachines(Collection<VirtualMachine> vms) {
		ec2Wrapper.stopVirtualMachines(vms);
		notifyOnUpdateVms(vms);
		vmUpdater.addVmsToStoppingPipeline(vms);
		return vms;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm) {
		List<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		deleteVirtualMachines(vms);
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
		Collection<VirtualMachine> terminatedVms = ec2Wrapper.deleteVirtualMachines(vms);
		// TODO deleting to deleted?
		notifyOnUpdateVms(terminatedVms);
		vmUpdater.addVmsToDeletingPipeline(vms);

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

	public void setUpdateListener(IUpdateListener<VirtualMachine> listener) {
		addVmUpdateListener(listener);
	}
}
