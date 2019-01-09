package com.ncc.savior.virtueadmin.cifsproxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.kerberos.client.KerberosRestTemplate;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService.PollHandler;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager.VirtueCreationDeletionListener;
import com.sun.security.jgss.ExtendedGSSContext;
import com.sun.security.jgss.ExtendedGSSCredential;

/**
 * Main manager class than handles creation and deletion of CIFs Proxies. There
 * should be one instance per user that is logged in or has an active virtue.
 */
public class CifsManager {
	private static final Logger logger = LoggerFactory.getLogger(CifsManager.class);
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
	private Client client;
	private ObjectMapper jsonMapper;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VirtueUser> cifsVmUpdater;

	public CifsManager(ServerIdProvider serverIdProvider, ICifsProxyDao cifsProxyDao, AwsEc2Wrapper wrapper,
			CompletableFutureServiceProvider serviceProvider, IVpcSubnetProvider vpcSubnetProvider,
			IKeyManager keyManager, String cifsProxyAmi, String cifsProxyLoginUser, String cifsKeyName,
			String instanceType) {
		this.cifsProxyDao = cifsProxyDao;
		this.wrapper = wrapper;
		this.serviceProvider = serviceProvider;
		this.serverId = serverIdProvider.getServerId();
		this.securityGroupIds = new ArrayList<String>();
		this.cifsKeyName = cifsKeyName;
		this.instanceType = InstanceType.fromValue(instanceType);
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.keyManager = keyManager;
		serviceProvider.getExecutor().scheduleWithFixedDelay(getTestForTimeoutRunnable(), 10000, 5000,
				TimeUnit.MILLISECONDS);

		this.cifsProxyVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "CifsProxyTemplate",
				OS.LINUX, cifsProxyAmi, new ArrayList<ApplicationDefinition>(), cifsProxyLoginUser, false, new Date(0),
				"system");
		client = ClientBuilder.newClient();
		this.jsonMapper = new ObjectMapper();
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
//		String baseCommand = "/usr/local/bin/post-deploy-config.sh";
		String baseCommand="~/post-deploy-config.sh";
		
		// kinit -k http/webserver.test.savior
		String cifsStart = String.format(
				"sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -jar cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&");

		// cf = serviceProvider.getAddRsa().chainFutures(cf, v);
		// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String cifsHostname = getHostname(vm);
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
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator("echo sleeping; sleep 300; echo slept;"));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator((myVm) -> {
			String principal = getPrincipal(vm);
			String cifsKinit = String.format("sudo kinit -k %s", principal);
			return cifsKinit;
		}));
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, new CommandGenerator(cifsStart));
		CommandGenerator cg = new CommandGenerator((myVm) -> {
			String cifsHostname = getHostname(vm);
			String cifsUpTest = String.format(
					"while ! curl http://%s:8080/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done; echo 'Connected'",
					cifsHostname);
			return cifsUpTest;
		});
		cg.setTimeoutMillis(300000);
		cf = serviceProvider.getRunRemoteCommand().chainFutures(cf, cg);
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

	private String getPrincipal(VirtualMachine vm) {
		String cifsHostname = getHostname(vm);
		String principal = String.format("http/%s.%s", cifsHostname, cifsDomain);
		return principal;
	}

	private String getHostname(VirtualMachine vm) {
		String cifsHostname = vm.getInternalHostname();
		cifsHostname = cifsHostname.replaceAll(".ec2.internal", "");
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

	protected void cifsOnVirtueCreation(VirtueInstance virtue, FileSystem fs) {
		try {
			VirtueUser user = userManager.getUser(virtue.getUsername());
			VirtualMachine vm = cifsProxyDao.getCifsVm(user);
			// create share if not created for user, store credentials
			// See cifs proxy documentation
			createShareOnCifsProxyVm(virtue, vm, user, fs);

			// should be hostname of file system.
			String fsName = fs.getAddress();
			String command = String.format(
					"sudo /usr/local/bin/allow-delegation.sh --domain %s --admin %s --password %s --delegater %s --target %s --verbose",
					cifsDomain, this.domainAdminUser, this.domainAdminUserPassword, this.getHostname(vm), fsName);
			CommandGenerator extra = new CommandGenerator(command);
			serviceProvider.getRunRemoteCommand().startFutures(vm, extra);
		} catch (Throwable t) {
			logger.error("error on cifs", t);
		}
	}

	private void createShareOnCifsProxyVm(VirtueInstance virtue, VirtualMachine vm, VirtueUser user, FileSystem fs) {
		try {
			String hostname = vm.getHostname();
			CifsShareCreationParameter cscp = new CifsShareCreationParameter(fs.getName(), fs.getId());

//			WebTarget t = client.target("http://" + hostname + ":8080").path("share");
//			Entity<?> entity = Entity.json(cscp);
//			byte[] token = getS4u2ProxyToken(user);
//			Response resp = t.request(MediaType.APPLICATION_JSON_TYPE).post(entity);
//			InputStream is = (InputStream) resp.getEntity();
//			status=resp.getStatus();
			ObjectNode node =jsonMapper.createObjectNode();
			ArrayNode permissions = jsonMapper.createArrayNode();
			String server="";
			String path="";
			
			node.put("name", fs.getName());
			node.put("virtueId", virtue.getId());
			node.put("server", server);
			node.put("path", path);
			node.set("permissions", permissions);
			node.put("type", "CIFS");
//			
			String password = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
			KerberosRestTemplate krt = new KerberosRestTemplate(null, user.getUsername(), password, null);
			String url = "http://" + hostname + ":8080/share";
			
			ResponseEntity<InputStream> resp = krt.postForEntity(url, cscp, InputStream.class);
			int status = resp.getStatusCodeValue();
			InputStream is = resp.getBody();

			if (status >= 400) {
				String body = IOUtils.toString(is, "UTF8");
				String msg = "Error creating share for CIFS Proxy.  Response code=" + status + " body=" + body;
				logger.error(msg);
				throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg);
			}

			CifsShareCreationParameter v = jsonMapper.readValue(is, CifsShareCreationParameter.class);
			logger.debug("CIFS returned " + v);
		} catch (IOException e) {
			String msg = "Error creating share for CIFS Proxy";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg, e);
		}
	}

	private byte[] getS4u2ProxyToken(VirtueUser user) throws GSSException {

		Oid krb5Mechanism = new Oid("1.2.840.113554.1.2.2");
		Oid krb5PrincipalNameType = new Oid("1.2.840.113554.1.2.2.1");
		int usage = GSSCredential.ACCEPT_ONLY;
		GSSManager manager = GSSManager.getInstance();
		GSSName userName = manager.createName(user.getUsername(), GSSName.NT_USER_NAME);

		try {
			ExtendedGSSCredential serviceCredentials1 = (ExtendedGSSCredential) manager.createCredential(userName,
					GSSCredential.DEFAULT_LIFETIME, krb5Mechanism, usage);
		} catch (Throwable t) {
			logger.error("Failed first attempt", t);
		}
		try {
			ExtendedGSSCredential serviceCredentials2 = (ExtendedGSSCredential) manager
					.createCredential(GSSCredential.INITIATE_ONLY);
		} catch (Throwable t) {
			logger.error("Failed second attempt", t);
		}
		try {
			securityServerName = "HTTP/EC2AMAZ-H6GG6ER@VIRTUE2.NCCDO.COM";
			GSSName serverName = manager.createName(this.securityServerName, krb5PrincipalNameType);

			ExtendedGSSCredential serviceCredentials = (ExtendedGSSCredential) manager.createCredential(serverName,
					GSSCredential.DEFAULT_LIFETIME, krb5Mechanism, GSSCredential.ACCEPT_ONLY);
			GSSCredential impersonatedUserCreds = ((ExtendedGSSCredential) serviceCredentials).impersonate(userName);

			final Oid KRB5_PRINCIPAL_OID = new Oid("1.2.840.113554.1.2.2.1");
			GSSName servicePrincipal = manager.createName("HTTP/webservice-host.domain.ltd", KRB5_PRINCIPAL_OID);
			ExtendedGSSContext extendedContext = (ExtendedGSSContext) manager.createContext(servicePrincipal,
					new Oid("1.3.6.1.5.5.2"), impersonatedUserCreds, GSSContext.DEFAULT_LIFETIME);
			final byte[] token = extendedContext.initSecContext(new byte[0], 0, 0);
//			return token;
		} catch (Throwable t) {
			logger.error("Failed again", t);

//			return null;
		}
		try {
			Oid koid = new Oid("1.2.840.113554.1.2.2");
			GSSName clientName = manager.createName("kdrumm@VIRTUE2.NCCDO.COM", GSSName.NT_USER_NAME);
			GSSCredential clientCred = manager.createCredential(clientName, 8 * 3600, koid,
					GSSCredential.INITIATE_ONLY);

			GSSName serverName = manager.createName("EC2AMAZ-H6GG6ER@VIRTUE2.NCCDO.COM", GSSName.NT_HOSTBASED_SERVICE);

			GSSContext context = manager.createContext(serverName, koid, clientCred, GSSContext.DEFAULT_LIFETIME);
			context.requestMutualAuth(true);
			context.requestConf(false);
			context.requestInteg(true);

			byte[] outToken = context.initSecContext(new byte[0], 0, 0);
			System.out.println(outToken);
			context.dispose();
		} catch (Throwable t) {
			logger.error("Failed again", t);

//			return null;
		}
		return null;
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

	public void addFilesystemLinux(VirtueUser user, Collection<FileSystem> fileSystems,
			Collection<VirtualMachine> myLinuxVms) {
		logger.debug("Adding filesystems " + fileSystems + " for vms " + myLinuxVms + " user=" + user);

		// get credentials of for all file systems for user
		for (VirtualMachine vm : myLinuxVms) {
			try {
				String keyName = vm.getPrivateKey();
				File privateKeyFile = keyManager.getKeyFileByName(keyName);
				Session session = SshUtil.getConnectedSession(vm, privateKeyFile);
				for (FileSystem fs : fileSystems) {
					//// servername/sharename /media/windowsshare cifs
					// credentials=/home/ubuntuusername/.smbcredentials,iocharset=utf8,sec=ntlm 0
					// 0
					String networkPath = fs.getAddress();
					String localPath = "/media/" + fs.getName();
					String script = String.format("echo '%s %s cifs defaults 0 2' >> /etc/fstab", networkPath,
							localPath);
					SshUtil.sendCommandFromSession(session, script);
				}
			} catch (JSchException | IOException e) {
				throw new SaviorException(SaviorErrorCode.SSH_ERROR,
						"Unable to connect to VM for FileSystem attachment, vm=" + vm);
			}
		}
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
				Collection<FileSystem> fileSystems = template.getFileSystems();
				for (FileSystem fs : fileSystems) {
					cifsOnVirtueCreation(virtue, fs);
				}
			}
		};
	}

	public void setActiveVirtueManager(ActiveVirtueManager activeVirtueManager) {
		this.activeVirtueManager = activeVirtueManager;
	}
}
