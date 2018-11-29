package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService.PollHandler;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager.VirtueCreationDeletionListener;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

/**
 * Main manager class than handles creation and deletion of CIFs Proxies. There
 * should be one instance per user that is logged in or has an active virtue.
 */
public class CifsManager {
	private static final Logger logger = LoggerFactory.getLogger(CifsManager.class);
	@Autowired
	private IUserManager userManager;
	private IActiveVirtueManager activeVirtueManager;
	private ICifsProxyDao cifsProxyDao;
	private AwsEc2Wrapper wrapper;
	private CompletableFutureServiceProvider serviceProvider;
	private VirtualMachineTemplate cifsProxyVmTemplate;
	private String cifsKeyName;
	private InstanceType instanceType;
	private String serverId;
	private ArrayList<String> securityGroupIds;

	@Value("${virtue.test:false}")
	private boolean test;

	public CifsManager(ServerIdProvider serverIdProvider, IActiveVirtueManager activeVirtueManager,
			DesktopVirtueService desktopService, ICifsProxyDao cifsProxyDao, AwsEc2Wrapper wrapper,
			CompletableFutureServiceProvider serviceProvider, String cifsProxyAmi, String cifsProxyLoginUser,
			String cifsKeyName, String instanceType) {
		this.activeVirtueManager = activeVirtueManager;
		this.cifsProxyDao = cifsProxyDao;
		this.wrapper = wrapper;
		this.serviceProvider = serviceProvider;
		this.serverId = serverIdProvider.getServerId();
		this.securityGroupIds = new ArrayList<String>();
		this.cifsKeyName = cifsKeyName;
		this.instanceType = InstanceType.fromValue(instanceType);
		serviceProvider.getExecutor().scheduleWithFixedDelay(getTestForTimeoutRunnable(), 10000, 5000,
				TimeUnit.MILLISECONDS);

		desktopService.addPollHandler(new PollHandler() {

			@Override
			public void onPoll(VirtueUser user, Map<String, VirtueTemplate> templates,
					Map<String, Set<VirtueInstance>> templateIdToActiveVirtues) {
				VirtualMachine vm = cifsProxyDao.getCifsVm(user);
				if (vm == null) {
					vm = createCifsProxyVm(user);
					cifsProxyDao.updateCifsVm(user, vm);
				}
				cifsProxyDao.updateUserTimeout(user);
			}
		});
		activeVirtueManager.addVirtueCreationDeletionListener(new VirtueCreationDeletionListener() {

			@Override
			public void onVirtueDeletion(VirtueInstance virtue) {
				VirtueUser user = userManager.getUser(virtue.getUsername());
				testAndShutdownCifs(user);
			}

			@Override
			public void onVirtueCreation(VirtueInstance virtue) {
				// do nothing
			}
		});
		this.cifsProxyVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "CifsProxyTemplate",
				OS.LINUX, cifsProxyAmi, new ArrayList<ApplicationDefinition>(), cifsProxyLoginUser, false, new Date(0),
				"system");

	}

	private void sync() {
		if (!test) {
			AmazonEC2 ec2 = wrapper.getEc2();
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
			List<Filter> filters = new ArrayList<Filter>();
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_PRIMARY)
					.withValues(VirtuePrimaryPurpose.CIFS_PROXY.toString()));
			describeInstancesRequest.withFilters(filters);
			DescribeInstancesResult result = ec2.describeInstances(describeInstancesRequest);
			List<Reservation> reservations = result.getReservations();
			Collection<VirtualMachine> allVmsInDb = cifsProxyDao.getAllCifsVms();
			List<String> instancesToBeTerminated = new ArrayList<String>();

			for (Reservation res : reservations) {
				for (Instance instance : res.getInstances()) {
					boolean match = false;
					String idOfAwsInstance = instance.getInstanceId();
					for (VirtualMachine vmInDb : allVmsInDb) {
						String dbId = vmInDb.getInfrastructureId();
						if (idOfAwsInstance.equals(dbId)) {
							match = true;
						}
					}
					if (!match) {
						instancesToBeTerminated.add(idOfAwsInstance);
					}
				}
			}
			logger.debug("sync is removing instances=" + instancesToBeTerminated);
			if (!instancesToBeTerminated.isEmpty()) {
				try {
					logger.debug(
							"Attempting to delete extra CIFS Proxy ec2 instances with ids=" + instancesToBeTerminated);
					TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(
							instancesToBeTerminated);
					wrapper.getEc2().terminateInstances(terminateInstancesRequest);
				} catch (AmazonEC2Exception e) {
					logger.warn("Failed to terminate extra CIFS Proxy ec2 instances.  InstanceIds="
							+ instancesToBeTerminated, e);
				}
			}
		}
	}

	protected VirtualMachine createCifsProxyVm(VirtueUser user) {
		logger.debug("Creating cifs proxy for user =" + user.getUsername());
		String name = "VRTU-Cifs-" + serverId + "-" + user.getUsername();
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters(name);
		virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.CIFS_PROXY);
		virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.CIFS_PROXY);
		VirtualMachine vm = wrapper.provisionVm(this.cifsProxyVmTemplate, name, securityGroupIds, cifsKeyName,
				instanceType, virtueMods, null);
		return vm;
	}

	private Runnable getTestForTimeoutRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				Set<VirtueUser> users = cifsProxyDao.getAllUsers();
				// logger.debug("Testing for timeouts for users=" + users);
				for (VirtueUser user : users) {
					testAndShutdownCifs(user);
				}
			}
		};
	}

	/**
	 * Tests if the CIFS proxy should be shutdown and does so if it should.
	 * 
	 * @param user
	 */
	protected void testAndShutdownCifs(VirtueUser user) {
		VirtualMachine vm = cifsProxyDao.getCifsVm(user);
		Collection<VirtueInstance> vs = activeVirtueManager.getVirtuesForUser(user);
		if (vm != null && vs.isEmpty() && userTimedOut(user)) {
			logger.debug("deleting proxy for user=" + user.getUsername());
			Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
			vms.add(vm);
			wrapper.deleteVirtualMachines(vms);
			cifsProxyDao.deleteCifsVm(user);
		}
	}

	private boolean userTimedOut(VirtueUser user) {
		long timeout = cifsProxyDao.getUserTimeout(user);
		long current = System.currentTimeMillis();
		return (current > timeout);
	}
}
