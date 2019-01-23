package com.ncc.savior.virtueadmin.cifsproxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.security.core.context.SecurityContextHolder;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.StringInputStream;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.BaseImediateCompletableFutureService;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.RunRemoteCommandCompletableFutureService.CommandGenerator;
import com.ncc.savior.virtueadmin.infrastructure.future.RunRemoteScriptCompletableFutureService.ScriptGenerator;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;
import com.ncc.savior.virtueadmin.model.CifsVirtueCreationParameter;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService.PollHandler;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager.VirtueCreationDeletionListener;

/**
 * Main manager class than handles creation and deletion of CIFs Proxies. There
 * should be one instance per user that is logged in or has an active virtue.
 */
public class CifsManager {
	private static final Logger logger = LoggerFactory.getLogger(CifsManager.class);
	private static final String CIFS_POST_DEPLOY_TEMPLATE = "cifs-post-deploy.tpl";
	@Autowired
	private IUserManager userManager;
	private ICifsProxyDao cifsProxyDao;
	private AwsEc2Wrapper wrapper;
	private CompletableFutureServiceProvider serviceProvider;
	private VirtualMachineTemplate cifsProxyVmTemplate;
	private String cifsKeyName;
	private InstanceType instanceType;
	private String serverId;
	private Collection<String> securityGroupIds;
	private IVpcSubnetProvider vpcSubnetProvider;
	private CifsProxyRestWrapper cifsRestWrapper;
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

	@Value("${savior.virtueadmin.principal}")
	private String securityServerName;

	@Value("${virtue.cifs.securityGroups}")
	private String securityGroupsString;
	@Value("${virtue.aws.server.subnet.name}")
	private String subnetName;
	private ActiveVirtueManager activeVirtueManager;
	private IKeyManager keyManager;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VirtueUser> cifsVmUpdater;
	private ITemplateService templateService;

	public CifsManager(ServerIdProvider serverIdProvider, ICifsProxyDao cifsProxyDao, AwsEc2Wrapper wrapper,
			CompletableFutureServiceProvider serviceProvider, IVpcSubnetProvider vpcSubnetProvider,
			IKeyManager keyManager, ITemplateService templateService, String cifsProxyAmi, String cifsProxyLoginUser,
			String cifsKeyName, String instanceType) {
		this.cifsProxyDao = cifsProxyDao;
		this.wrapper = wrapper;
		this.serviceProvider = serviceProvider;
		this.serverId = serverIdProvider.getServerId();
		this.securityGroupIds = new ArrayList<String>();
		this.cifsKeyName = cifsKeyName;
		this.instanceType = InstanceType.fromValue(instanceType);
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.keyManager = keyManager;
		this.templateService = templateService;
		this.cifsRestWrapper = new CifsProxyRestWrapper();
		serviceProvider.getExecutor().scheduleWithFixedDelay(getTestForTimeoutRunnable(), 10000, 5000,
				TimeUnit.MILLISECONDS);

		this.cifsProxyVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "CifsProxyTemplate",
				OS.LINUX, cifsProxyAmi, new ArrayList<ApplicationDefinition>(), cifsProxyLoginUser, false, new Date(0),
				"system");
		this.cifsVmUpdater = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VirtueUser>(
				"CifsProxyUpdater") {

			@Override
			protected VirtualMachine onExecute(VirtualMachine param, VirtueUser user) {
				cifsProxyDao.updateCifsVm(user, param);
				return param;
			}
		};
	}

	public PollHandler getPollHandler() {
		return new PollHandler() {

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
		};
	}

	/**
	 * CIFS proxy actions that need to be performed when a virtue creation is
	 * starting.
	 * 
	 * @param virtue
	 * @param fs
	 * @return
	 */
	public CifsVirtueCreationParameter cifsBeforeVirtueCreation(VirtueInstance virtue,
			Collection<FileSystem> fileSystems) {
		// TODO should be run in another thread to avoid stopping the virtue startup. or
		// at least cause virtue to go to error state if fails.
		try {
			VirtueUser user = userManager.getUser(virtue.getUsername());
			VirtualMachine cifsVm = cifsProxyDao.getCifsVm(user);
			String cifsProxyHostname = this.getHostnameFromDns(cifsVm);
			String password = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
			CifsVirtueCreationParameter virtueParams = cifsRestWrapper.createVirtue(cifsProxyHostname,
					user.getUsername(), password, virtue);

			for (FileSystem fs : fileSystems) {
				String delegator = cifsProxyHostname;
				String target = getHostnameFromShareAddress(fs.getAddress());

				HashMap<String, Object> model = new HashMap<String, Object>();
				model.put("cifsDomain", cifsDomain);
				model.put("domainAdmin", domainAdminUser);
				model.put("domainPassword", domainAdminUserPassword);
				model.put("cifsAdUrl", cifsAdUrl);
				model.put("domainIp", domainIp);
				model.put("delegator", delegator);
				model.put("target", target);
				model.put("fileSystem", fs);
				model.put("cifsVm", cifsVm);
				model.put("user", user);

				File keyFile = keyManager.getKeyFileByName(cifsVm.getPrivateKeyName());
				JSch ssh = new JSch();
				Session session;
				ssh.addIdentity(keyFile.getAbsolutePath());
				session = ssh.getSession(cifsVm.getUserName(), cifsVm.getIpAddress(), cifsVm.getSshPort());
				session.setConfig("PreferredAuthentications", "publickey");
				session.setConfig("StrictHostKeyChecking", "no");
				session.setTimeout(1000);
				session.connect();
				SshUtil.runCommandsFromFile(templateService, session, "cifs-allow-delegation.tpl", model);
				// cifsProxyDao.saveVirtueParams(virtueParams);
				CifsShareCreationParameter share = cifsRestWrapper.createShare(cifsProxyHostname, user.getUsername(),
						password, virtue.getId(), fs);
				share.setFileSystemId(fs.getId());
				cifsProxyDao.saveShareParams(share);
			}

			return virtueParams;

		} catch (JSchException | TemplateException | IOException t) {
			logger.error("error on cifs", t);
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, "Error creating cifs virtue", t);
		}
	}

	public void cifsBeforeVirtueDelete(VirtueInstance virtueInstance) {
		String password = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
		VirtueUser user = userManager.getUser(virtueInstance.getUsername());
		VirtualMachine cifsVm = cifsProxyDao.getCifsVm(user);
		String cifsHostname = getHostnameFromDns(cifsVm);
		List<CifsShareCreationParameter> shares = cifsProxyDao.getSharesForVirtue(virtueInstance.getId());
		for (CifsShareCreationParameter share : shares) {
			cifsRestWrapper.deleteShare(cifsHostname, user.getUsername(), password, share.getExportedName());
		}
		// Do we need to delete virtues?
	}

	/**
	 * Performed after virtue creation has completed.
	 * 
	 * @param virtue
	 * @param user
	 * @param fileSystems
	 * @param myLinuxVms
	 * @param password
	 */
	public void addFilesystemLinux(VirtueInstance virtue, VirtueUser user, Collection<FileSystem> fileSystems,
			Collection<VirtualMachine> myLinuxVms, String password) {
		VirtualMachine cifsVm = cifsProxyDao.getCifsVm(user);
		String cifsProxyHostname = getHostnameFromDns(cifsVm);

		CifsVirtueCreationParameter cifsVirtueParams = cifsRestWrapper.getVirtueParams(cifsProxyHostname,
				user.getUsername(), password, virtue.getId());
		logger.debug("Adding filesystems " + fileSystems + " for vms " + myLinuxVms + " user=" + user);
		List<CifsShareCreationParameter> shares = cifsProxyDao.getSharesForVirtue(virtue.getId());
		// copying values so we can reuse. Shares from database can't be iterated over
		// more than once.
		shares = new ArrayList<CifsShareCreationParameter>(shares);
		// get credentials of for all file systems for user
		for (VirtualMachine vm : myLinuxVms) {
			try {
				String keyName = vm.getPrivateKeyName();
				File privateKeyFile = keyManager.getKeyFileByName(keyName);
				Session session = SshUtil.getConnectedSession(vm, privateKeyFile);
				for (CifsShareCreationParameter share : shares) {
					runFileSystemLinuxScripts(cifsVirtueParams, cifsVm, session, share);
				}
			} catch (JSchException | IOException e) {
				throw new SaviorException(SaviorErrorCode.SSH_ERROR,
						"Unable to connect to VM for FileSystem attachment, vm=" + vm);
			}
		}
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
		addVmToProvisionPipeline(vm, user, new CompletableFuture<VirtualMachine>());
		return vm;
	}

	private void addVmToProvisionPipeline(VirtualMachine vm, VirtueUser user,
			CompletableFuture<VirtualMachine> future) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(vm, v);
		cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
		cf = serviceProvider.getEnsureDeleteVolumeOnTermination().chainFutures(cf, v);
		cf = cifsVmUpdater.chainFutures(cf, user);
		// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
		cf = addScriptsToRunLaterTemplated(vm, cf);
//		cf = addScriptsToRunLaterOld(vm, cf);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		cf = cifsVmUpdater.chainFutures(cf, user);
		cf.thenAccept((VirtualMachine myVm) -> {
			logger.debug("CIFS Proxy future complete.  Vm="+myVm);
			future.complete(myVm);
		});
		cf.exceptionally((ex) -> {
			logger.error("EXCEPTION", ex);
			future.completeExceptionally(ex);
			vm.setState(VmState.ERROR);
			return vm;
		});
	}

	private CompletableFuture<VirtualMachine> addScriptsToRunLaterTemplated(VirtualMachine vm,
			CompletableFuture<VirtualMachine> cf) {
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("cifsDomain", cifsDomain);
		model.put("domainAdmin", domainAdminUser);
		model.put("domainPassword", domainAdminUserPassword);
		model.put("cifsAdUrl", cifsAdUrl);
		model.put("domainIp", domainIp);
		model.put("cifsPort", 8080);

		ScriptGenerator scriptGenerator = new ScriptGenerator((myVm) -> {
			String cifsHostname = getHostnameFromDns(myVm);
			model.put("cifsHostname", cifsHostname);
			String principal = getPrincipal(vm);
			model.put("principal", principal);
			try {
				String[] commands = templateService.processTemplateToLines(CIFS_POST_DEPLOY_TEMPLATE, model);
				return commands;
			} catch (TemplateException e) {
				String msg = "Template Service error while trying to run cifs post deploy script";
				logger.error(msg, e);
				throw new SaviorException(SaviorErrorCode.TEMPLATE_ERROR, msg);
			}
		});
//		scriptGenerator.setDryRun(true);
		cf = serviceProvider.getRunRemoteScript().chainFutures(cf, scriptGenerator);
		return cf;
	}

	private CompletableFuture<VirtualMachine> addScriptsToRunLaterOld(VirtualMachine vm,
			CompletableFuture<VirtualMachine> cf) {
		// String baseCommand = "/usr/local/bin/post-deploy-config.sh";
		String baseCommand = "/usr/local/bin/post-deploy-config.sh";

		// kinit -k http/webserver.test.savior
		String cifsStart = String.format(
				"sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -jar cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&");

		// cf = serviceProvider.getAddRsa().chainFutures(cf, v);
		// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String cifsHostname = getHostnameFromDns(vm);
			String cifsPostDeploy = String.format(
					"sudo %s --domain %s --admin %s --password %s --hostname %s --dcip %s --verbose &> post-deploy.log",
					baseCommand, cifsDomain, domainAdminUser, domainAdminUserPassword, cifsHostname, domainIp);
			return cifsPostDeploy;
		}));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator("$?"));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String principal = getPrincipal(vm);
			String cifPropertyUpdate = String.format("echo 'savior.cifsproxy.principal=%s' > cifs-proxy.properties",
					principal);
			return cifPropertyUpdate;
		}));
		String cifPropertyUpdate = String
				.format("echo 'savior.security.ad.domain=%s' >> cifs-proxy-security.properties;", cifsDomain);
		cifPropertyUpdate += String.format("echo 'savior.security.ad.url=%s' >> cifs-proxy-security.properties;",
				cifsAdUrl);
		cifPropertyUpdate += String.format("echo 'savior.security.ldap=%s' >> cifs-proxy-security.properties;",
				cifsAdUrl);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator(cifPropertyUpdate));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator("cat *.properties"));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf,
				new CommandGenerator("echo sleeping; sleep 20; echo slept;"));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String principal = getPrincipal(vm);
			String cifsKinit = String.format("sudo kinit -k %s", principal);
			return cifsKinit;
		}));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator(cifsStart));
		CommandGenerator cg = new CommandGenerator((myVm) -> {
			String cifsHostname = getHostnameFromDns(vm);
			String cifsUpTest = String.format(
					"while ! curl http://%s:8080/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done; echo 'Connected'",
					cifsHostname);
			return cifsUpTest;
		});
		cg.setTimeoutMillis(300000);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, cg);
		return cf;
	}

	private String getPrincipal(VirtualMachine vm) {
		String cifsHostname = getHostnameFromDns(vm);
		String principal = String.format("http/%s.%s", cifsHostname, cifsDomain);
		return principal;
	}

	private String getHostnameFromDns(VirtualMachine vm) {
		String cifsHostname = vm.getInternalHostname();
		int index = cifsHostname.indexOf(".");
		if (index > 0) {
			return cifsHostname.substring(0, index);
		}
//		cifsHostname = cifsHostname.replaceAll(".ec2.internal", "");
		return cifsHostname;
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

	public static String getHostnameFromShareAddress(String target) {
		target = target.replaceAll("\\\\", "/");
		while (target.startsWith("/")) {
			target = target.substring(1);
		}
		int index = target.indexOf("/");
		if (index > 0) {
			target = target.substring(0, index);
		}

		return target;
	}

	public static String getPathFromShareAddress(String target) {
		target = target.replaceAll("\\\\", "/");
		while (target.startsWith("/")) {
			target = target.substring(1);
		}
		int index = target.indexOf("/");
		if (index > 0) {
			target = target.substring(index);
		}
		return target;
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
		if (vm != null && vs.isEmpty() && isUserTimedOut(user)) {
			logger.debug("deleting proxy for user=" + user.getUsername());
			Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
			vms.add(vm);
			wrapper.deleteVirtualMachines(vms);
			cifsProxyDao.deleteCifsVm(user);
		}
	}

	private boolean isUserTimedOut(VirtueUser user) {
		long timeout = cifsProxyDao.getUserTimeout(user);
		long current = System.currentTimeMillis();
		return (current > timeout);
	}

	// TODO make into templated scripts!
	private void runFileSystemLinuxScripts(CifsVirtueCreationParameter cifsVirtueParams, VirtualMachine cifsVm,
			Session session, CifsShareCreationParameter share) throws JSchException, IOException {
		String exportedName = share.getExportedName();
		String localPath = "/media/" + exportedName;
		String networkPath = "//" + cifsVm.getInternalIpAddress() + "/" + exportedName;

		String credentialFileName = Long.toString(share.getId());
		String credentialsDir = "/home/user/.smbcredentials/";
		String credentialFilePath = credentialsDir + credentialFileName;

		String test = String.format("password=%s\n", cifsVirtueParams.getPassword());

		String setupScript = String.format("mkdir %s; sudo mkdir %s ;", credentialsDir, localPath);
//		String script1 = String.format("echo '' >> %s", credentialFilePath);
		String script2 = String.format("echo 'username=%s' >> %s", cifsVirtueParams.getUsername(), credentialFilePath);
//		String script2 = String.format("cat >> %s; %s", credentialFilePath, cifsVirtueParams.getPassword());
		String script3 = String.format("sudo sh -c \"echo '%s %s cifs credentials=%s 0 2' >> /etc/fstab\"", networkPath,
				localPath, credentialFilePath);
		String script4 = String.format("sudo mount %s", localPath);

		StringInputStream source = new StringInputStream(test);

		SshUtil.sendCommandFromSession(session, setupScript);
		try {
			SshUtil.sftpFile(session, source, credentialFilePath);
		} catch (SftpException e) {
			throw new IOException("Error FTP'ing password file.", e);
		}
//		SshUtil.sendCommandFromSession(session, script1);
		SshUtil.sendCommandFromSession(session, script2);
		SshUtil.sendCommandFromSession(session, script3);
		List<String> mountput = SshUtil.sendCommandFromSession(session, script4);
		logger.debug("mountput: " + mountput);
	}

	public VirtueCreationDeletionListener getVirtueCreationDeletionListener() {
		return new VirtueCreationDeletionListener() {

			@Override
			public void onVirtueDeletion(VirtueInstance virtue) {
				VirtueUser user = userManager.getUser(virtue.getUsername());
				testAndShutdownCifs(user);
			}

			@Override
			public void onVirtueCreation(VirtueInstance virtue, VirtueTemplate template) {
				// do nothing
			}
		};
	}

	public void setActiveVirtueManager(ActiveVirtueManager activeVirtueManager) {
		this.activeVirtueManager = activeVirtueManager;
	}
}
