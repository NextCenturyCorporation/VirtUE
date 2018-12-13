package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.RunRemoteCommandCompletableFutureService.CommandGenerator;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
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
	private Collection<String> securityGroupIds;
	private IVpcSubnetProvider vpcSubnetProvider;
	private String subnetId;

	@Value("${virtue.test:false}")
	private boolean test;
	@Value("${virtue.cifs.domain.user}")
	private String domainAdminUser;
	@Value("${virtue.cifs.domain.password}")
	private String domainAdminUserPassword;
	@Value("${virtue.cifs.domain.ip}")
	private String domainIp;
	@Value("${virtue.cufs.domain.name}")
	private String cifsDomain;
	@Value("${virtue.cifs.domain.url}")
	private String cifsAdUrl;

	@Value("${virtue.cifs.securityGroups}")
	private String securityGroupsString;
	@Value("${virtue.aws.server.subnet.name}")
	private String subnetName;

	public CifsManager(ServerIdProvider serverIdProvider, IActiveVirtueManager activeVirtueManager,
			DesktopVirtueService desktopService, ICifsProxyDao cifsProxyDao, AwsEc2Wrapper wrapper,
			CompletableFutureServiceProvider serviceProvider, IVpcSubnetProvider vpcSubnetProvider, String cifsProxyAmi,
			String cifsProxyLoginUser, String cifsKeyName, String instanceType) {
		this.activeVirtueManager = activeVirtueManager;
		this.cifsProxyDao = cifsProxyDao;
		this.wrapper = wrapper;
		this.serviceProvider = serviceProvider;
		this.serverId = serverIdProvider.getServerId();
		this.securityGroupIds = new ArrayList<String>();
		this.cifsKeyName = cifsKeyName;
		this.instanceType = InstanceType.fromValue(instanceType);
		this.vpcSubnetProvider = vpcSubnetProvider;
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

	private static Collection<String> splitOnComma(String securityGroupsCommaSeparated) {
		Collection<String> groups = new ArrayList<String>();
		if (securityGroupsCommaSeparated != null) {
			for (String group : securityGroupsCommaSeparated.split(",")) {
				groups.add(group.trim());
			}
		}
		return groups;
	}

	protected void sync() {
		String vpcId = vpcSubnetProvider.getVpcId();
		this.subnetId = AwsUtil.getSubnetIdFromName(vpcId, subnetName, wrapper);
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(splitOnComma(securityGroupsString), vpcId,
				wrapper);
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
		logger.debug("Creating cifs proxy for user=" + user.getUsername());
		String name = "VRTU-Cifs-" + serverId + "-" + user.getUsername();
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters(name);
		virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.CIFS_PROXY);
		virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.CIFS_PROXY);
		virtueMods.setUsername(user.getUsername());
		virtueMods.setSubnetId(subnetId);
		VirtualMachine vm = wrapper.provisionVm(this.cifsProxyVmTemplate, name, securityGroupIds, cifsKeyName,
				instanceType, virtueMods, null);
		addVmToProvisionPipeline(vm, new CompletableFuture<VirtualMachine>());
		return vm;
	}

	private void addVmToProvisionPipeline(VirtualMachine vm, CompletableFuture<VirtualMachine> future) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(vm, v);
		cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
		cf = serviceProvider.getEnsureDeleteVolumeOnTermination().chainFutures(cf, v);
		// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
		String baseCommand = "/usr/local/bin/post-deploy-config.sh";

		// kinit -k http/webserver.test.savior
		String cifsStart = String.format(
				"sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -jar cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&");

		// cf = serviceProvider.getAddRsa().chainFutures(cf, v);
		// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String cifsHostname = vm.getInternalHostname();
			cifsHostname = cifsHostname.replaceAll(".ec2.internal", "");
			String cifsPostDeploy = String.format(
					"sudo %s --domain %s --admin %s --password %s --hostname %s --dcip %s --verbose &> post-deploy.log",
					baseCommand, cifsDomain, domainAdminUser, domainAdminUserPassword, cifsHostname, domainIp);
			return cifsPostDeploy;
		}));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String cifsHostname = vm.getInternalHostname();
			cifsHostname = cifsHostname.replaceAll(".ec2.internal", "");
			String principal = String.format("http/%s.%s", cifsHostname, cifsDomain);
			String cifPropertyUpdate = String.format("echo 'savior.cifsproxy.principal=%s' > cifs-proxy.properties",
					principal);
			return cifPropertyUpdate;
		}));
		String cifPropertyUpdate = String.format("echo 'savior.security.ad.domain=%s' >> cifs-proxy-security.properties;",
				cifsDomain);
		cifPropertyUpdate += String.format("echo 'savior.security.ad.url=%s' >> cifs-proxy-security.properties;",
				cifsAdUrl);
		cifPropertyUpdate += String.format("echo 'savior.security.ldap=%s' >> cifs-proxy-security.properties;",
				cifsAdUrl);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator(cifPropertyUpdate));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator("cat *.properties"));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String cifsHostname = vm.getInternalHostname();
			cifsHostname = cifsHostname.replaceAll(".ec2.internal", "");
			String principal = String.format("http/%s.%s", cifsHostname, cifsDomain);
			String cifsKinit = String.format("sudo kinit -k %s", principal);
			return cifsKinit;
		}));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator(cifsStart));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String cifsHostname = vm.getInternalHostname();
			cifsHostname = cifsHostname.replaceAll(".ec2.internal", "");
			String cifsUpTest = String.format(
					"while ! curl http://%s:8080/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done", cifsHostname);
			return cifsUpTest;
		}));
		cf.thenAccept((VirtualMachine myVm) -> {
			logger.debug("CIFS Proxy future complete");
			future.complete(myVm);
		});
		cf.exceptionally((ex) -> {
			logger.error("EXCEPTION", ex);
			future.completeExceptionally(ex);
			vm.setState(VmState.ERROR);
			return vm;
		});
	}

	private void addToDeletePipeline(VirtualMachine vm, CompletableFuture<VirtualMachine> future) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getUpdateStatus().startFutures(vm, VmState.DELETING);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, false);
		cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.DELETED);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf.thenAccept((myVm) -> {
			future.complete(myVm);
		});
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
