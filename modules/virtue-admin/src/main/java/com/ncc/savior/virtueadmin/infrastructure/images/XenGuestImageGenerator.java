package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.appstream.model.ImageState;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.infrastructure.mixed.BaseXenTemplateProvider;
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
	private static final String BASE_IMAGE = "base-image";
	private static final String S3_UPLOAD_MAIN_CLASS = "com.ncc.savior.server.s3.S3Upload";
	private static final Logger logger = LoggerFactory.getLogger(XenGuestImageGenerator.class);
	private Collection<String> securityGroupIds;
	private String systemUser = "System";
	private AwsEc2Wrapper ec2Wrapper;
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
	private String xenLoginUser;

	private IPackageInstaller packageInstaller;

	// this is the dom0 linux, not domu linux
	@Value("${virtue.imageCreation.initialAmi.dom0Linux}")
	private String baseLinuxAmi;
	@Value("${virtue.imageCreation.initialAmi.dom0}")
	private String linuxWithDom0Ami;
	@Value("${virtue.imageCreation.initialAmi.domULinux}")
	private String domULinux;
	private BaseXenTemplateProvider xenImageProvider;
	private Integer dom0SizeGb=64;

	public XenGuestImageGenerator(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper,
			IVpcSubnetProvider vpcSubnetProvider, CompletableFutureServiceProvider serviceProvider,
			BaseXenTemplateProvider xenImageProvider, IPackageInstaller packageInstaller, String xenInstanceTypeStr,
			String securityGroupsCommaSeparated, String iamRoleName, String xenKeyName, String xenLoginUser,
			String subnetName, String region, String bucket, String kmsKey) {
		super();
		this.packageInstaller = packageInstaller;
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
		this.region = region;
		this.bucket = bucket;
		this.kmsKey = kmsKey;
		this.xenLoginUser = xenLoginUser;
		this.xenImageProvider = xenImageProvider;

	}

	public void init() {
		sync();
//		startDom0TestThread();
		Thread t = new Thread(() -> {
			try {

				String oldAmi = xenImageProvider.getXenVmTemplate().getTemplatePath();
				startDom0AndBaseTestThread().join();
				String newAmi = xenImageProvider.getXenVmTemplate().getTemplatePath();
				logger.debug("old=" + oldAmi + " new=" + newAmi);
				if (oldAmi.equals(newAmi)) {
					logger.error("FAILED!");
				} else {
					startSnapshotTestThread(null);
				}
			} catch (InterruptedException e) {
				logger.error("Test failed", e);
			}
		});
//		 t.start();
	}

	private void startDom0TestThread() {
		new Thread(() -> {
			try {
				ImageDescriptor desc = new ImageDescriptor("XenDom0-" + System.currentTimeMillis());
				// this is the magical AMI to use for ubuntu.
				desc.setBaseDomUAmi("ami-0ac019f4fcb7cb7e6");
				CompletableFuture<Dom0ImageResult> future = createNewDom0Ami(desc);
				future.get();
				logger.debug("test done");
			} catch (InterruptedException | ExecutionException e) {
				logger.debug("test failed", e);
			}
		}).start();
	}

	private Thread startDom0AndBaseTestThread() {
		Thread t = new Thread(() -> {
			try {
				ImageDescriptor desc = new ImageDescriptor("XenDomUBase-" + System.currentTimeMillis());
				// desc.setDom0Ami("ami-00adf65830abfd276");
				desc.setDom0Ami("ami-06a471b7c5341b049");
				// this is the magical AMI to use for ubuntu. It is an Ubuntu 18.04 amd64 bionic
				// image from awas
//				desc.setBaseDomUAmi("ami-0ac019f4fcb7cb7e6");

				CompletableFuture<Dom0ImageResult> future = createNewDomUBaseImage(desc);
				Dom0ImageResult result = future.get();
				xenImageProvider.setXenVmTemplateFromAmi(result.getAmi());
				logger.debug("test done " + result.getAmi());
				logger.debug("Saved template AMI: " + xenImageProvider.getXenVmTemplate().getTemplatePath());
			} catch (InterruptedException | ExecutionException e) {
				logger.debug("test failed", e);
			}
		});
		t.start();
		return t;
	}

	private void startSnapshotTestThread(String xenAmi) {
		new Thread(() -> {
			// while (true) {
			try {
				String templatePath = "domUSnap-" + System.currentTimeMillis();
				ImageDescriptor desc = new ImageDescriptor(templatePath);
				if (xenAmi != null) {
					desc.setBaseDomUAmi(xenAmi);
				}
				// this is the magical AMI to use for ubuntu.
//				desc.setBaseDomUAmi("ami-0ac019f4fcb7cb7e6");
				List<String> apps = new ArrayList<String>();
				apps.add(ImageDescriptor.FIREFOX);
				apps.add(ImageDescriptor.GNOME_CALCULATOR);
				apps.add(ImageDescriptor.XTERM);
				desc.setAppKeys(apps);

				ImageResult image = createNewDomUSnapshotImage(desc).get();
				logger.debug(image.toString());
			} catch (InterruptedException | ExecutionException e) {
				logger.error("error creating snapshot ", e);
			}
			// }
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

	public CompletableFuture<Dom0ImageResult> createNewDom0Ami(ImageDescriptor imageDescriptor) {
		CompletableFuture<Dom0ImageResult> imageResultFuture = new CompletableFuture<Dom0ImageResult>();
		String linuxAmi = (imageDescriptor.getBaseLinuxAmi() != null ? imageDescriptor.getBaseLinuxAmi()
				: baseLinuxAmi);
		CompletableFuture<VirtualMachine> vmFuture = getDom0Vm(linuxAmi, "Xen-Dom0", dom0SizeGb);
		vmFuture.handle((xenVm, ex) -> {
			if (ex != null) {
				logger.error("Error creating Dom0!", ex);
			} else {
				// happy path
				try {
					ImageBuildStage stage = ImageBuildStage.dom0;
					File privateKeyFile = keyManager.getKeyFileByName(xenVm.getPrivateKeyName());
					Session sshDom0Session = SshUtil.getConnectedSession(xenVm, privateKeyFile);
					Map<String, Object> dataModel = new HashMap<String, Object>();
					dataModel.put("xenVm", xenVm);
					// scripts?
					runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.dom0,
							ImageBuildModifer.prior.toString());
					// run ansible
					packageInstaller.installPackages(stage, imageDescriptor, dataModel);

					runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.dom0,
							ImageBuildModifer.prior.toString());
					sshDom0Session.disconnect();
					Dom0ImageResult result = saveAmi(imageDescriptor, xenVm);
					imageResultFuture.complete(result);
				} catch (Exception e) {
					logger.error("error in image creation", e);
					imageResultFuture.completeExceptionally(e);
				}
				logger.debug("attempting to delete Dom0 image creation VM=" + xenVm.getName() + " id="
						+ xenVm.getInfrastructureId());
				Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
				vms.add(xenVm);
				ec2Wrapper.deleteVirtualMachines(vms);
			}
			return xenVm;
		});
		return imageResultFuture;
	}

	private void runScript(Session sshDom0Session, Map<String, Object> dataModel, ImageBuildStage stage,
			ImageBuildTarget target, String script) throws JSchException, IOException, TemplateException {
		if (script == null) {
			script = "default";
		}
		String template = "image/" + stage + "/" + target + "-" + script + ".tpl";
		List<String> lines = SshUtil.runScriptFromFile(templateService, sshDom0Session, template, dataModel);
		String output = lines.stream().collect(Collectors.joining("\n  "));
		logger.debug("output for script for stage=" + stage + " target=" + target + " script=" + script + " template="
				+ template + "\n  " + output);
	}

	public CompletableFuture<Dom0ImageResult> createNewDomUBaseImage(ImageDescriptor imageDescriptor) {
		if (!JavaUtil.isNotEmpty(imageDescriptor.getDom0Ami())) {
			imageDescriptor.setDom0Ami(linuxWithDom0Ami);
		}
		if (JavaUtil.isNotEmpty(imageDescriptor.getDom0Ami())) {
			return createNewDomUBaseImageDirect(imageDescriptor);
		} else {
			// We don't have a Dom0 image yet, we need to create it!
			CompletableFuture<Dom0ImageResult> future = createNewDom0Ami(imageDescriptor);

			return future.thenApply((ret) -> {
				Dom0ImageResult result;
				try {
					result = future.get();
					imageDescriptor.setDom0Ami(result.getAmi());
					Dom0ImageResult finalResult = createNewDomUBaseImageDirect(imageDescriptor).get();
					xenImageProvider.setXenVmTemplateFromAmi(finalResult.getAmi());
					return finalResult;
				} catch (InterruptedException | ExecutionException e) {
					throw new SaviorException(SaviorErrorCode.AWS_ERROR,
							"Error creating Dom0 image with DomU base image");
				}
			});
		}
	}

	private CompletableFuture<Dom0ImageResult> createNewDomUBaseImageDirect(ImageDescriptor imageDescriptor) {
		CompletableFuture<Dom0ImageResult> imageResultFuture = new CompletableFuture<Dom0ImageResult>();
		ImageBuildStage stage = ImageBuildStage.domuBase;
		String dom0Ami = imageDescriptor.getDom0Ami() != null ? imageDescriptor.getDom0Ami() : linuxWithDom0Ami;
		String suffix = "DomUBase";
		CompletableFuture<VirtualMachine> vmFuture = getDom0Vm(dom0Ami, suffix, dom0SizeGb);

		// create domU Image from linux ami
		// first create basic debian
		if (imageDescriptor.getBaseDomUAmi() == null) {
			imageDescriptor.setBaseDomUAmi(domULinux);
		}
		CompletableFuture<VirtualMachine> createDomUAmiFuture = createBasicLinuxAwsInstance(
				imageDescriptor.getBaseDomUAmi(), suffix,null);

		// once created, run ansible on it.
		CompletableFuture<VirtualMachine> domUtoS3Future = new CompletableFuture<VirtualMachine>();
		createDomUAmiFuture.handle((domUVm, ex) -> {
			if (ex != null) {
				logger.error("Error creating DomU!", ex);
				imageResultFuture.completeExceptionally(ex);
			} else {
				try {
					File privateKeyFile = keyManager.getKeyFileByName(domUVm.getPrivateKeyName());
					Session sshDom0Session = SshUtil.getConnectedSession(domUVm, privateKeyFile);
					Map<String, Object> dataModel = new HashMap<String, Object>();
					dataModel.put("domUVm", domUVm);
					dataModel.put("bucket", bucket);
					runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.domu,
							ImageBuildModifer.prior.toString());
					// run ansible

					ImageBuildStage substage = ImageBuildStage.domuBaseCreateDomu;
					packageInstaller.installPackages(substage, imageDescriptor, dataModel);

					// runScript(sshDomuSession, dataModel, stage,
					// ImageBuildTarget.domu,
					// ImageBuildModifer.post.toString());
					runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.domu,
							ImageBuildModifer.post.toString());
					sshDom0Session.disconnect();
					domUtoS3Future.complete(domUVm);
				} catch (Exception e) {
					logger.error("error in image creation", e);
					domUtoS3Future.completeExceptionally(e);
				}
				logger.debug("attempting to delete Dom0 image creation VM=" + domUVm.getName() + " id="
						+ domUVm.getInfrastructureId());
				Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
				vms.add(domUVm);
				ec2Wrapper.deleteVirtualMachines(vms);
			}
			return domUVm;
		});

		// once ansible completed on the domU run next set

		domUtoS3Future.handle((domUVm, domUEx) -> {
			if (domUEx != null) {
				logger.error("Error creating Dom0!", domUEx);
			} else {
				vmFuture.handle((xenVm, ex) -> {
					if (ex != null) {
						logger.error("Error creating Dom0!", ex);
						imageResultFuture.completeExceptionally(ex);
					} else {
						// happy path
						try {
							File privateKeyFile = keyManager.getKeyFileByName(xenVm.getPrivateKeyName());
							Session sshDom0Session = SshUtil.getConnectedSession(xenVm, privateKeyFile);
							Map<String, Object> dataModel = new HashMap<String, Object>();
							dataModel.put("xenVm", xenVm);
							dataModel.put("basePath", BASE_IMAGE);
							dataModel.put("bucket", bucket);
							runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.dom0,
									ImageBuildModifer.prior.toString());

							// run ansible
							packageInstaller.installPackages(stage, imageDescriptor, dataModel);

							// runScript(sshDomuSession, dataModel, stage,
							// ImageBuildTarget.domu,
							// ImageBuildModifer.post.toString());
							runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.dom0,
									ImageBuildModifer.post.toString());
							sshDom0Session.disconnect();

							Dom0ImageResult result = saveAmi(imageDescriptor, xenVm);
							imageResultFuture.complete(result);
						} catch (Exception e) {
							logger.error("error in image creation", e);
							imageResultFuture.completeExceptionally(e);
						}
						logger.debug("attempting to delete Dom0 image creation VM=" + xenVm.getName() + " id="
								+ xenVm.getInfrastructureId());
						Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
						vms.add(xenVm);
						ec2Wrapper.deleteVirtualMachines(vms);
					}
					// vmFuture.handle return, result is not used
					return xenVm;
				});
			}
			// domUToS3Future.handle return, result is not used
			return domUVm;
		});
		return imageResultFuture;
	}

	private CompletableFuture<VirtualMachine> createBasicLinuxAwsInstance(String ubuntuAmi, String suffix, Integer diskSizeGb) {
		// TODO user should be configurable
		VirtualMachineTemplate vmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "debianTemplate",
				OS.LINUX, ubuntuAmi, Collections.emptyList(), "ubuntu", true, new Date(), "System");
		return getVm(ubuntuAmi, suffix + "-domU", vmTemplate, xenInstanceType, iamRoleName, diskSizeGb);
	}

	public CompletableFuture<ImageResult> createNewDomUSnapshotImage(ImageDescriptor imageDescriptor) {
		if (!JavaUtil.isNotEmpty(imageDescriptor.getBaseDomUAmi())) {
			imageDescriptor.setBaseDomUAmi(xenImageProvider.getXenVmTemplate().getTemplatePath());
		}
		if (JavaUtil.isNotEmpty(imageDescriptor.getBaseDomUAmi())) {
			return createNewDomUSnapshotImageDirect(imageDescriptor);
		} else {
			// We don't have a DomUbase image yet, we need to create it!
			CompletableFuture<Dom0ImageResult> future = createNewDomUBaseImage(imageDescriptor);

			return future.thenApply((ret) -> {
				Dom0ImageResult result;
				try {
					result = future.get();
					imageDescriptor.setDom0Ami(result.getAmi());
					return createNewDomUSnapshotImageDirect(imageDescriptor).get();
				} catch (InterruptedException | ExecutionException e) {
					throw new SaviorException(SaviorErrorCode.AWS_ERROR,
							"Error creating Dom0 image with DomU base image");
				}
			});
		}
	}

	private CompletableFuture<ImageResult> createNewDomUSnapshotImageDirect(ImageDescriptor imageDescriptor) {
		String imageLoginUser = "user";
		ImageBuildStage stage = ImageBuildStage.domuSnapshot;
		// get Dom0Vm
		String baseDomUAmi = imageDescriptor.getBaseDomUAmi() != null ? imageDescriptor.getBaseDomUAmi()
				: this.xenImageProvider.getXenVmTemplate().getTemplatePath();
		CompletableFuture<VirtualMachine> vmFuture = getDom0Vm(baseDomUAmi, "DomUSnapshot", dom0SizeGb);
		CompletableFuture<ImageResult> imageResultFuture = new CompletableFuture<ImageResult>();

		vmFuture.handle((xenVm, ex) -> {
			if (ex == null) {
				try {
					logger.info("Starting image creation:");
					File privateKeyFile = keyManager.getKeyFileByName(xenVm.getPrivateKeyName());
					Session sshDom0Session = SshUtil.getConnectedSession(xenVm, privateKeyFile);
					Map<String, Object> dataModel = new HashMap<String, Object>();
					dataModel.put("xenVm", xenVm);
					dataModel.put("basePath", BASE_IMAGE);

					dataModel.put("bucket", bucket);
					dataModel.put("encryptionKey", kmsKey);
					dataModel.put("s3Folder", imageDescriptor.getTemplatePath());

					// "firefox gnome-terminal"
					String apps = imageDescriptor.getAppKeys().stream().collect(Collectors.joining(" "));
					dataModel.put("apps", apps);
					// run pre-dom0 scripts
					runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.dom0,
							ImageBuildModifer.prior.toString());
					// start base DomU
					// VirtualMachine xenGuestVm = startDomU(xenVm, imageLoginUser, sshDom0Session);

					VirtualMachine xenGuestVm = new VirtualMachine("", "", null, null, null, null, xenVm.getHostname(),
							8001, imageLoginUser, xenVm.getPrivateKey(), xenVm.getPrivateKeyName(),
							xenVm.getIpAddress());
					dataModel.put("guestVm", xenGuestVm);

					// Session sshDomUSession = getStableSession(privateKeyFile, xenGuestVm);

					// run pre-domU scripts
					// runScript(sshDomUSession, dataModel, stage, ImageBuildTarget.domu,
					// ImageBuildModifer.prior.toString());
					// run ansible
					packageInstaller.installPackages(stage, imageDescriptor, dataModel);
					// run post-domU scripts
					// runScript(sshDomUSession, dataModel, stage, ImageBuildTarget.domu,
					// ImageBuildModifer.post.toString());

					// shutdownAndWaitGuest(sshDom0Session, dataModel);

					// run post-dom0 scripts
					runScript(sshDom0Session, dataModel, stage, ImageBuildTarget.dom0,
							ImageBuildModifer.post.toString());
					// push files
					String templatePath = imageDescriptor.getTemplatePath();
					dataModel.put("mainClass", S3_UPLOAD_MAIN_CLASS);
					dataModel.put("region", region);
					dataModel.put("kmsKey", kmsKey);
					dataModel.put("bucket", bucket);
					dataModel.put("templatePath", templatePath);
					// List<String> lines = SshUtil.runScriptFromFile(templateService,
					// sshDom0Session, "image/upload.tpl",
					// dataModel);
					// if (logger.isTraceEnabled()) {
					// logger.trace("Upload output: " + lines);
					// } else {
					// logger.debug("Upload complete");
					// }
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
				logger.error("Error getting Dom0 for snapshot image creation.", ex);
			}
			return xenVm;
		});
		return imageResultFuture;
	}

	private void shutdownAndWaitGuest(Session sshDom0Session, Map<String, Object> dataModel)
			throws TemplateException, JSchException, IOException {
		// shutdown domU
		List<String> lines = SshUtil.runCommandsFromFile(templateService, sshDom0Session, "image/xenGuestStop.tpl",
				dataModel);
		if (logger.isTraceEnabled()) {
			logger.trace("Stop output: " + lines);
		}
		// wait for shutdown
		// ????
		lines = SshUtil.runScriptFromFile(templateService, sshDom0Session, "image/waitForShutdown.tpl", dataModel);
		logger.debug("wait for shutdown result: " + lines);
	}

	private Dom0ImageResult saveAmi(ImageDescriptor imageDescriptor, VirtualMachine xenVm) {
		logger.debug("Saving dom0 AMI from instance " + xenVm.getInfrastructureId());
		CreateImageRequest createImageRequest = new CreateImageRequest(xenVm.getInfrastructureId(),
				imageDescriptor.getTemplatePath());
		createImageRequest.setNoReboot(false);
		CreateImageResult awsResult = ec2Wrapper.getEc2().createImage(createImageRequest);
		String ami = awsResult.getImageId();
		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		Collection<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(AwsUtil.TAG_NAME, imageDescriptor.getTemplatePath()));
		tags.add(new Tag(AwsUtil.TAG_PRIMARY, VirtuePrimaryPurpose.IMAGE_CREATION.toString()));
		tags.add(new Tag(AwsUtil.TAG_SERVER_ID, serverId));
		tags.add(new Tag(AwsUtil.TAG_AUTO_GENERATED, AwsUtil.TAG_AUTO_GENERATED_TRUE));
		createTagsRequest.withTags(tags).withResources(ami);
		ec2Wrapper.getEc2().createTags(createTagsRequest);
		logger.debug("Dom0 image save started with AMI=" + ami);
		Dom0ImageResult result = new Dom0ImageResult(ami);
		waitUntilAmiReady(ami);
		logger.debug("Dom0 image save completed with AMI=" + ami);
		return result;
	}

	private void waitUntilAmiReady(String ami) {
		// TODO timeout?
		DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
		Collection<String> imageIds = new ArrayList<String>();
		imageIds.add(ami);
		describeImagesRequest.setImageIds(imageIds);
		while (true) {
			DescribeImagesResult result = ec2Wrapper.getEc2().describeImages(describeImagesRequest);
			List<Image> images = result.getImages();
			if (images.isEmpty()) {
				throw new SaviorException(SaviorErrorCode.AWS_ERROR, "Unable to find AMI=" + ami);
			}
			Image image = images.get(0);
			ImageState status = ImageState.valueOf(image.getState().toUpperCase());
			if (ImageState.PENDING.equals(status)) {
				JavaUtil.sleepAndLogInterruption(1000);
				continue;
			} else if (ImageState.AVAILABLE.equals(status)) {
				break;
			} else {
				throw new SaviorException(SaviorErrorCode.AWS_ERROR,
						"AMI creation failed with ami=" + ami + " and state=" + status);
			}
		}
	}

	private Session getStableSession(File privateKeyFile, VirtualMachine xenGuestVm) {
		Session sshDomUSession;
		// for some reason, the domU session isn't stable at first. This gets stable
		// session.
		while (true) {
			try {
				logger.debug("trying to create domU session");
				sshDomUSession = SshUtil.getConnectedSessionWithRetries(xenGuestVm, privateKeyFile, 5, 500);
				SshUtil.sendCommandFromSession(sshDomUSession, "echo 'session test' > session.log");
				break;
			} catch (Exception e) {
				JavaUtil.sleepAndLogInterruption(500);
				continue;
			}
		}
		return sshDomUSession;
	}

	private VirtualMachine startDomU(VirtualMachine xenVm, String guestLoginUser, Session dom0Session)
			throws TemplateException, JSchException, IOException {
		XenHostManager.waitUntilXlListIsReady(dom0Session);
		String infrastructureId = "id";
		int externalSshPort = 8001;

		VirtualMachine guestVm = new VirtualMachine(UUID.randomUUID().toString(), xenVm.getName(),
				new ArrayList<ApplicationDefinition>(0), VmState.CREATING, OS.LINUX, infrastructureId,
				xenVm.getHostname(), externalSshPort, guestLoginUser, "", xenKeyName, xenVm.getIpAddress());
		String ipAddress = "0.0.0.0";
		// String name = xenVm.getName();
		// TODO this shouldn't be here, but we need to modify the AMI.
		SshUtil.sendCommandFromSession(dom0Session,
				"mv ~/app-domains/master-orig/swap.qcow2 ~/app-domains/base-image/; mv ~/app-domains/master-orig/*generic* ~/app-domains/base-image/;");
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("xenVm", xenVm);
		model.put("templatePath", BASE_IMAGE);
		model.put("securityRole", "god");
		List<String> lines = SshUtil.runCommandsFromFile(templateService, dom0Session, "image/xenGuestCreate.tpl",
				model);
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

	private CompletableFuture<VirtualMachine> getDom0Vm(String ami, String postfix,Integer sizeGb) {
		VirtualMachineTemplate vmTemplate = getXenVmWithBaseImageTemplate(ami);
		return getVm(ami, postfix + "-dom0", vmTemplate, xenInstanceType, iamRoleName, sizeGb);
	}

	private CompletableFuture<VirtualMachine> getVm(String ami, String postfix, VirtualMachineTemplate vmTemplate,
			InstanceType xenInstanceType, String iamRoleName, Integer sizeGb) {
		Collection<String> secGroupIds = new HashSet<String>(securityGroupIds);
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters("Image-Creation");
		if (virtueMods.getSecurityGroupId() != null) {
			secGroupIds.add(virtueMods.getSecurityGroupId());
		}
		// virtueMods.setVirtueId(virtue.getId());
		// virtueMods.setVirtueTemplateId(virtue.getTemplateId());
		virtueMods.setDiskSizeGB(sizeGb);
		virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.IMAGE_CREATION);
		virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.XEN_HOST);
		virtueMods.setUsername(systemUser);
		virtueMods.setSubnetId(subnetId);
		VirtualMachine vm = ec2Wrapper.provisionVm(vmTemplate, "VRTU-ImageCreation-" + serverId + "-" + postfix,
				secGroupIds, xenKeyName, xenInstanceType, virtueMods, iamRoleName);
		CompletableFuture<VirtualMachine> vmFuture = new CompletableFuture<VirtualMachine>();
		addVmToProvisionPipeline(vm, vmFuture);
		return vmFuture;
	}

	private VirtualMachineTemplate getXenVmWithBaseImageTemplate(String ami) {
		return new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate-ImageCreation", OS.LINUX, ami,
				new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), systemUser);
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
		// cf = serviceProvider.getAddRsa().chainFutures(cf, v);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf.handle((vm, ex) -> {
			if (ex == null) {
				logger.debug("xen host future complete");
				xenFuture.complete(vm);
				return vm;
			} else {
				logger.error("EXCEPTION", ex);
				xenFuture.completeExceptionally(ex);
				xenVm.setState(VmState.ERROR);
				return xenVm;
			}
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

	/**
	 * The VM we are going to run the script on
	 */
	public static enum ImageBuildTarget {
		dom0, domu
	}

	/**
	 * The portion that the current task is trying to create
	 */
	public static enum ImageBuildStage {
		dom0, domuBase, domuSnapshot, domuBaseCreateDomu
	}

	public static enum ImageBuildModifer {
		prior, post
	}
}
