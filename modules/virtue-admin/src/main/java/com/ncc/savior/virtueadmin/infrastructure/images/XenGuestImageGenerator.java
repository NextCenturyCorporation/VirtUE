package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.infrastructure.mixed.XenGuestManager;
import com.ncc.savior.virtueadmin.infrastructure.mixed.XenHostManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

public class XenGuestImageGenerator {
	private static final String S3_UPLOAD_MAIN_CLASS = "com.ncc.savior.server.s3.S3Download";
	private static final Logger logger = LoggerFactory.getLogger(XenGuestImageGenerator.class);
	private Collection<String> securityGroupIds;
	private String systemUser = "System";
	private AwsEc2Wrapper ec2Wrapper;
	private VirtualMachineTemplate xenVmTemplate;
	private String serverId;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private String iamRoleName;
	// private String xenAmi;
	// private String xenLoginUser;
	private CompletableFutureServiceProvider serviceProvider;

	@Autowired
	ITemplateService templateService;

	@Autowired
	IKeyManager keyManager;
	private String subnetId;

	@Value("${virtue.test:false}")
	private boolean test;
	private String region;
	private String kmsKey;
	private String bucket;

	public XenGuestImageGenerator(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper,
			IVpcSubnetProvider vpcSubnetProvider, CompletableFutureServiceProvider serviceProvider, String xenAmi,
			String xenInstanceTypeStr, String securityGroupsCommaSeparated, String iamRoleName, String xenKeyName,
			String xenLoginUser, String subnetName, String region, String bucket, String kmsKey) {
		super();
		serverId = serverIdProvider.getServerId();
		String vpcId = vpcSubnetProvider.getVpcId();
		this.subnetId = AwsUtil.getSubnetIdFromName(vpcId, subnetName, ec2Wrapper);
		Collection<String> securityGroupsNames = splitOnComma(securityGroupsCommaSeparated);
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(securityGroupsNames, vpcId, ec2Wrapper);
		this.ec2Wrapper = ec2Wrapper;
		this.serviceProvider = serviceProvider;
		// this.xenAmi = xenAmi;
		// this.xenLoginUser = xenLoginUser;
		this.xenKeyName = xenKeyName;
		this.xenInstanceType = InstanceType.fromValue(xenInstanceTypeStr);
		this.iamRoleName = iamRoleName;
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate-ImageCreation",
				OS.LINUX, xenAmi, new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), systemUser);
	}

	public void init() {
		sync();

		// TODO remove this thread this is only for easy testing
		// TODO still remove this thread
		new Thread(() -> {
			while (true) {
				try {
					String templatePath = "test-" + System.currentTimeMillis();
					ImageResult image = createNewDomUImage(new ImageDescriptor(templatePath)).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void sync() {
		if (!test) {
			// remove any image generator VM's at startup since we can't result;
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
			Collection<Filter> filters = new ArrayList<Filter>();
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_PRIMARY)
					.withValues(VirtuePrimaryPurpose.IMAGE_CREATION.toString()));
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SECONDARY)
					.withValues(VirtueSecondaryPurpose.XEN_HOST.toString()));
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
			filters.add(new Filter(AwsUtil.FILTER_STATE_NAME).withValues(AwsUtil.STATE_RUNNING, AwsUtil.STATE_PENDING,
					AwsUtil.STATE_STOPPING, AwsUtil.STATE_STOPPED));
			describeInstancesRequest.withFilters(filters);
			DescribeInstancesResult result = ec2Wrapper.getEc2().describeInstances(describeInstancesRequest);
			List<String> instanceIds = new ArrayList<String>();
			for (Reservation res : result.getReservations()) {
				for (Instance inst : res.getInstances()) {
					instanceIds.add(inst.getInstanceId());
				}
			}
			if (!instanceIds.isEmpty()) {
				logger.info("Clearing extraneous Xen Guest Image creation VMs: " + instanceIds);
				TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
				ec2Wrapper.getEc2().terminateInstances(terminateInstancesRequest);
			}
		}
	}

	public CompletableFuture<ImageResult> createNewDomUImage(ImageDescriptor imageDescriptor) {
		String imageLoginUser = "user";
		// get Dom0Vm
		CompletableFuture<VirtualMachine> vmFuture = getDomOVm();
		CompletableFuture<ImageResult> imageResultFuture = new CompletableFuture<ImageResult>();

		vmFuture.handle((xenVm, ex) -> {
			if (ex == null) {
				try {
					logger.info("Starting image creation:");
					File privateKeyFile = keyManager.getKeyFileByName(xenVm.getPrivateKeyName());
					Session sshDom0Session = SshUtil.getConnectedSession(xenVm, privateKeyFile);
					Map<String, Object> dataModel = new HashMap<String, Object>();
					dataModel.put("xenVm", xenVm);
					// run pre-dom0 scripts
					List<String> lines = SshUtil.runScriptFromFile(templateService, sshDom0Session,
							"image/imageCreation-Dom0Before.tpl", dataModel);
					// start base DomU
					VirtualMachine xenGuestVm = startDomU(xenVm, imageLoginUser, sshDom0Session);
					dataModel.put("guestVm", xenGuestVm);
					JavaUtil.sleepAndLogInterruption(4000);
					Session sshDomUSession = SshUtil.getConnectedSessionWithRetries(xenGuestVm, privateKeyFile, 5, 500);
					// run pre-domU scripts
					lines = SshUtil.runScriptFromFile(templateService, sshDomUSession,
							"image/imageCreation-DomUBefore.tpl", dataModel);
					// run ansible
					// TODO ANSIBLE
					// run post-domU scripts
					lines = SshUtil.runScriptFromFile(templateService, sshDomUSession,
							"image/imageCreation-DomUAfter.tpl", dataModel);
					// run post-dom0 scripts
					lines = SshUtil.runScriptFromFile(templateService, sshDom0Session,
							"image/imageCreation-Dom0After.tpl", dataModel);
					// shutdown domU
					dataModel.put("vm", xenGuestVm);
					lines = SshUtil.runCommandsFromFile(templateService, sshDom0Session, "xen-guest-stop.tpl",
							dataModel);

					// push files
					String templatePath = imageDescriptor.getTemplatePath();
					dataModel.put("mainClass", S3_UPLOAD_MAIN_CLASS);
					dataModel.put("region", region);
					dataModel.put("kmsKey", kmsKey);
					dataModel.put("bucket", bucket);
					dataModel.put("templatePath", templatePath);
					lines = SshUtil.runScriptFromFile(templateService, sshDom0Session, "image/imageCreation-upload.tpl",
							dataModel);
					ImageResult imageResult = new ImageResult(templatePath);
					imageResultFuture.complete(imageResult);
				} catch (JSchException | IOException | TemplateException e) {
					logger.error("error in image creation", e);
					imageResultFuture.completeExceptionally(e);
				}
				Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
				vms.add(xenVm);
				ec2Wrapper.deleteVirtualMachines(vms);
			} else {

			}
			return xenVm;
		});
		return imageResultFuture;
	}

	private VirtualMachine startDomU(VirtualMachine xenVm, String guestLoginUser, Session dom0Session)
			throws TemplateException, JSchException, IOException {
		XenHostManager.waitUntilXlListIsReady(dom0Session);
		String infrastructureId = "id";
		int externalSshPort = 8001;

		VirtualMachine guestVm = new VirtualMachine(UUID.randomUUID().toString(), "Image",
				new ArrayList<ApplicationDefinition>(0), VmState.CREATING, OS.LINUX, infrastructureId,
				xenVm.getHostname(), externalSshPort, guestLoginUser, "", xenKeyName, xenVm.getIpAddress());
		String ipAddress = "0.0.0.0";
		// String name = xenVm.getName();
		// TODO this shouldn't be here, but we need to modify the AMI.
		SshUtil.sendCommandFromSession(dom0Session,
				"mv ~/app-domains/master-orig/swap.qcow2 ~/app-domains/base-image/; mv ~/app-domains/master-orig/*generic* ~/app-domains/base-image/;");
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("vm", xenVm);
		model.put("templatePath", "base-image");
		model.put("securityRole", "god");
		List<String> lines = SshUtil.runCommandsFromFile(templateService, dom0Session, "xen-guest-create.tpl", model);
		logger.debug("provision guest output: " + lines);
		ipAddress = XenGuestManager.getGuestVmIpAddress(dom0Session, xenVm.getName());

		int internalPort = 22;
		model.put("internalSshPort", internalPort);
		model.put("externalDns", xenVm.getHostname());
		model.put("xenInternalIpAddress", ipAddress);
		model.put("vm", guestVm);
		lines = SshUtil.runCommandsFromFileWithTimeout(templateService, dom0Session, "Dom0-sshForwarding.tpl", model,
				200);
		return guestVm;
	}

	private CompletableFuture<VirtualMachine> getDomOVm() {
		Collection<String> secGroupIds = new HashSet<String>(securityGroupIds);
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters("Image-Creation");
		if (virtueMods.getSecurityGroupId() != null) {
			secGroupIds.add(virtueMods.getSecurityGroupId());
		}
		// virtueMods.setVirtueId(virtue.getId());
		// virtueMods.setVirtueTemplateId(virtue.getTemplateId());
		virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.IMAGE_CREATION);
		virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.XEN_HOST);
		virtueMods.setUsername(systemUser);
		virtueMods.setSubnetId(subnetId);
		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate, "VRTU-ImageCreation-" + serverId, secGroupIds,
				xenKeyName, xenInstanceType, virtueMods, iamRoleName);
		CompletableFuture<VirtualMachine> xenFuture = new CompletableFuture<VirtualMachine>();
		addVmToProvisionPipeline(xenVm, xenFuture);
		return xenFuture;
	}

	private void addVmToProvisionPipeline(VirtualMachine xenVm, CompletableFuture<VirtualMachine> xenFuture) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(xenVm, v);
		cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
		cf = serviceProvider.getEnsureDeleteVolumeOnTermination().chainFutures(cf, v);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		// cf = serviceProvider.getErrorCausingService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
		cf = serviceProvider.getAddRsa().chainFutures(cf, v);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf.thenAccept((VirtualMachine vm) -> {
			logger.debug("xen host future complete");
			xenFuture.complete(vm);
		});
		cf.exceptionally((ex) -> {
			logger.error("EXCEPTION", ex);
			xenFuture.completeExceptionally(ex);
			xenVm.setState(VmState.ERROR);
			return xenVm;
		});
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
}
