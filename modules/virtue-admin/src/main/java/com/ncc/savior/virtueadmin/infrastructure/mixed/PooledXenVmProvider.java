/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

public class PooledXenVmProvider implements IXenVmProvider {
	private static final Logger logger = LoggerFactory.getLogger(PooledXenVmProvider.class);
	private IVpcSubnetProvider vpcSubnetProvider;
	private AwsEc2Wrapper ec2Wrapper;
	private VirtualMachineTemplate xenVmTemplate;
	private String iamRoleName;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private String serverId;
	private BlockingDeque<VirtualMachine> pool;
	private Collection<String> securityGroups;
	protected IActiveVirtueDao xenVmDao;
	private int poolSize;
	private StandardXenProvider nonPool;
	private CompletableFutureServiceProvider serviceProvider;

	public PooledXenVmProvider(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper,
			IVpcSubnetProvider vpcSubnetProvider, IActiveVirtueDao xenVmDao,
			CompletableFutureServiceProvider serviceProvider, String xenAmi, String xenLoginUser, String xenKeyName,
			InstanceType xenInstanceType, String iamRoleName, Collection<String> securityGroupsNames, int poolSize) {
		this.pool = new LinkedBlockingDeque<VirtualMachine>();
		this.serverId = serverIdProvider.getServerId();
		this.ec2Wrapper = ec2Wrapper;
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.xenKeyName = xenKeyName;
		this.iamRoleName = iamRoleName;
		this.xenInstanceType = xenInstanceType;
		this.poolSize = poolSize;
		this.xenVmDao = xenVmDao;
		this.serviceProvider = serviceProvider;
		String vpcId = vpcSubnetProvider.getVpcId();
		this.securityGroups = AwsUtil.getSecurityGroupIdsByNameAndVpcId(securityGroupsNames, vpcId, ec2Wrapper);
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX, xenAmi,
				new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), "system");
		this.nonPool = new StandardXenProvider(serverIdProvider, ec2Wrapper, vpcSubnetProvider, xenAmi, xenLoginUser,
				xenKeyName, xenInstanceType, iamRoleName);
		initPool();
	}

	private void initPool() {
		if (poolSize > 0) {
			List<VirtualMachine> oldVms = xenVmDao.getVmWithNameStartsWith(VM_NAME_POOL_PREFIX);
			// make sure all the ones we were using are still running
			for (VirtualMachine vm : oldVms) {
				try {
					/*
					 * Can't just get them all at once because if any of them don't exist any more,
					 * AWS throws an exception.
					 */
					AwsUtil.updateStatusOnVm(ec2Wrapper.getEc2(), vm);
				} catch (AmazonEC2Exception e) {
					logger.debug("could not get status for vm " + vm.getId() + ": " + e);
					vm.setState(VmState.ERROR);
				}
			}
			Collection<VirtualMachine> runningVms = new ArrayList<>();
			Collection<VirtualMachine> nonRunningVms = new ArrayList<>();
			for (VirtualMachine vm : oldVms) {
				if (vm.getState() == VmState.RUNNING) {
					runningVms.add(vm);
				} else {
					nonRunningVms.add(vm);
				}
			}
			Collection<VirtualMachine> deletedVms = ec2Wrapper.deleteVirtualMachines(nonRunningVms);
			if (deletedVms.size() != nonRunningVms.size()) {
				// couldn't delete all of them for some reason
				Collection<VirtualMachine> undeletableVms = new ArrayList<>(nonRunningVms);
				undeletableVms.removeAll(deletedVms);
				logger.warn(
						"Could not delete some non-running VMs from the pool. Will try again next time the server restarts: "
								+ undeletableVms);
			}
			/*
			 * Only forget about ones we actually deleted, so we can try again later
			 * (otherwise if there's a transient failure when deleting them, they would just
			 * hang around forever).
			 */
			deletedVms.forEach(xenVmDao::deleteVm);

			if (logger.isDebugEnabled()) {
				List<String> vmStrings = runningVms.stream()
						.map((vm) -> vm.getId() + "[" + vm.getInfrastructureId() + ", " + vm.getState() + "]")
						.collect(Collectors.toList());
				logger.debug("using pre-existing VMs for the pool: " + vmStrings);
			}
			pool.addAll(runningVms);
			int xensNeeded = poolSize - pool.size();
			if (xensNeeded > 0) {
				new Thread(() -> {
					JavaUtil.sleepAndLogInterruption(2000);
					for (int i = 0; i < xensNeeded; i++) {
						provisionToQueue();
					}
				}, "XenPoolInsertion-initial").start();
			}
		}
	}

	public PooledXenVmProvider(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper,
			IVpcSubnetProvider vpcSubnetProvider, IActiveVirtueDao xenVmDao,
			CompletableFutureServiceProvider serviceProvider, String xenAmi, String xenLoginUser, String xenKeyName,
			String xenInstanceType, String iamRoleName, String securityGroupsCommaSeparated, int poolSize) {
		this(serverIdProvider, ec2Wrapper, vpcSubnetProvider, xenVmDao, serviceProvider, xenAmi, xenLoginUser,
				xenKeyName, InstanceType.fromValue(xenInstanceType), iamRoleName,
				splitOnComma(securityGroupsCommaSeparated), poolSize);

	}

	private static Collection<String> splitOnComma(String securityGroupsCommaSeparated) {
		Collection<String> groups = new ArrayList<String>();
		if (securityGroupsCommaSeparated != null) {
			for (String group : securityGroupsCommaSeparated.split(",")) {
				groups.add(group.trim());
			}
		}
		return groups;
	}

	@Override
	public VirtualMachine getNewXenVm(VirtueInstance vi, VirtueCreationAdditionalParameters virtueMods,
			String virtueName, Collection<String> secGroupIds) {
		if (poolSize < 1) {
			return nonPool.getNewXenVm(vi, virtueMods, virtueName, secGroupIds);
		} else {
			return getXenFromPool(vi, virtueMods, virtueName, secGroupIds);
		}
	}

	private synchronized VirtualMachine getXenFromPool(VirtueInstance vi, VirtueCreationAdditionalParameters virtueMods,
			String virtueName, Collection<String> secGroupIds) {
		AmazonEC2 ec2 = ec2Wrapper.getEc2();
		String oldSubnetKey = null;
		String name = "VRTU-Xen-" + serverId + "-" + vi.getUsername() + "-" + virtueName;
		VirtualMachine xenVm = getNextPoolVm();
		provisionToQueueAsync();
		xenVm.setName(name);
		xenVmDao.updateVms(Collections.singletonList(xenVm));

		DescribeInstancesRequest dir = new DescribeInstancesRequest();
		dir.withInstanceIds(xenVm.getInfrastructureId());
		DescribeInstancesResult inst = ec2.describeInstances(dir);
		oldSubnetKey = xenVm.getId();
		String subnetId = inst.getReservations().get(0).getInstances().get(0).getSubnetId();
		virtueMods.setSubnetId(subnetId);

		// Get instance
		// Assign security groups
		ModifyInstanceAttributeRequest modifyInstanceAttributeRequest = new ModifyInstanceAttributeRequest();
		modifyInstanceAttributeRequest.withGroups(secGroupIds).withInstanceId(xenVm.getInfrastructureId());
		ec2.modifyInstanceAttribute(modifyInstanceAttributeRequest);

		List<Tag> tags = ec2Wrapper.getTagsFromVirtueMods(xenVmTemplate.getId(), name, virtueMods, xenVm.getId());

		List<Tag> subnetTags = new ArrayList<Tag>();
		subnetTags.add(new Tag(AwsUtil.TAG_USERNAME, vi.getUsername()));
		subnetTags.add(new Tag(AwsUtil.TAG_VIRTUE_NAME, vi.getName()));
		subnetTags.add(new Tag(AwsUtil.TAG_VIRTUE_INSTANCE_ID, vi.getId()));
		subnetTags.add(new Tag(AwsUtil.TAG_VIRTUE_TEMPLATE_ID, vi.getTemplateId()));
		subnetTags.add(new Tag(AwsUtil.TAG_NAME, vi.getUsername() + "-" + vi.getName()));

		tags.add(new Tag(AwsUtil.TAG_USERNAME, vi.getUsername()));
		tags.add(new Tag(AwsUtil.TAG_VIRTUE_NAME, vi.getName()));
		tags.add(new Tag(AwsUtil.TAG_VIRTUE_INSTANCE_ID, vi.getId()));
		tags.add(new Tag(AwsUtil.TAG_VIRTUE_TEMPLATE_ID, vi.getTemplateId()));
		tags.add(new Tag(AwsUtil.TAG_NAME, name));

		vpcSubnetProvider.reassignSubnet(oldSubnetKey, vi.getId(), subnetTags);

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(xenVm.getInfrastructureId()).withTags(tags);
		ec2.createTags(createTagsRequest);
		return xenVm;
	}

	protected VirtualMachine getNextPoolVm() {
		try {
			logger.debug("Waiting for VM from pool.  size=" + pool.size());
			VirtualMachine vm = pool.take();
			logger.debug("Got vm from pool.  VM=" + vm);
			return vm;
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
			throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, "Interrupted while waiting for xen pool vm", e);
		}
	}

	private List<Instance> getPoolInstances() {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		Collection<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
		filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_PRIMARY)
				.withValues(VirtuePrimaryPurpose.XEN_POOL.toString()));
		request.setFilters(filters);
		DescribeInstancesResult result = ec2Wrapper.getEc2().describeInstances(request);
		List<Instance> pool = new ArrayList<Instance>();
		List<Reservation> reses = result.getReservations();
		for (Reservation res : reses) {
			pool.addAll(res.getInstances());
		}
		return pool;
	}

	protected void provisionToQueueAsync() {
		Runnable run = () -> {
			provisionToQueue();
		};
		Thread t = new Thread(run, "XenPoolInsertion");
		t.setDaemon(true);
		t.start();
	}

	private synchronized void provisionToQueue() {
		logger.debug("Adding vm to pool...");
		try {
			VirtualMachine xenVm = provisionNewXenVm();
			pool.add(xenVm);
			xenVmDao.updateVms(Collections.singletonList(xenVm));
		} catch (RuntimeException e) {
			logger.error("could not create new VM for pool", e);
		}
		logger.debug("...added vm to pool.  Size=" + pool.size());
	}

	protected VirtualMachine provisionNewXenVm() {
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters("XEN_POOL");

		Map<String, String> tags = new HashMap<String, String>();
		tags.put(AwsUtil.TAG_NAME, "XEN_POOL");
		tags.put(AwsUtil.TAG_VIRTUE_NAME, "XEN_POOL");

		String id = UUID.randomUUID().toString();
		String subnetId = vpcSubnetProvider.getSubnetId(id, tags);

		virtueMods.setSubnetId(subnetId);
		virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.XEN_POOL);
		virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.XEN_HOST);

		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate, VM_NAME_POOL_PREFIX + serverId, securityGroups,
				xenKeyName, xenInstanceType, virtueMods, iamRoleName);
		try {
			vpcSubnetProvider.reassignSubnet(id, xenVm.getId(), null);

			CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			createTagsRequest.withResources(subnetId).withTags(new Tag(AwsUtil.TAG_VIRTUE_INSTANCE_ID, xenVm.getId()));
			ec2Wrapper.getEc2().createTags(createTagsRequest);
		} catch (AmazonEC2Exception | SaviorException e) {
			logger.error("failed to configure VM, so deleting it", e);
			ec2Wrapper.deleteVirtualMachines(Collections.singletonList(xenVm));
			throw e;
		}
		return xenVm;
	}

	// Facilitates clearing the pool.
	@Override
	public synchronized void setXenPoolSize(int newPoolSize) {
		if (newPoolSize < 0) {
			throw new SaviorException(SaviorErrorCode.INVALID_INPUT,
					"Invalid pool size.  Pool size cannot be set to " + newPoolSize);
		}
		int oldPoolSize = poolSize;
		int instancesToBeAdded = newPoolSize - oldPoolSize;
		if (instancesToBeAdded > 0) {
			for (int i = 0; i < instancesToBeAdded; i++) {
				provisionToQueueAsync();
			}
		} else if (instancesToBeAdded < 0) {
			for (int i = 0; i < -instancesToBeAdded; i++) {
				deleteInstanceFromQueue();
			}
		}
		this.poolSize = newPoolSize;
	}

	private synchronized void deleteInstanceFromQueue() {
		if (!pool.isEmpty()) {
			VirtualMachine vm = pool.removeLast();
			String subnetKey = vm.getId();
			Set<VirtualMachine> vms = Collections.singleton(vm);
			ec2Wrapper.deleteVirtualMachines(vms);
			xenVmDao.deleteVm(vm);
			CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(vm, false);
			cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.DELETED);
			cf.thenRun(() -> {
				vpcSubnetProvider.releaseBySubnetKey(subnetKey);
			});
		}
	}

	@Override
	public int getXenPoolSize() {
		return poolSize;
	}
}
