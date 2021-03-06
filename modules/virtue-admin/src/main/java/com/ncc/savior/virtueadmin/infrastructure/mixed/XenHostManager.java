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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.FutureCombiner;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.infrastructure.persistent.PersistentStorageManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

/**
 * This class handles creation, deletion, start, and stop among other management
 * task of Xen Host Virtual Machines (Dom0). It uses {@link AwsEc2Wrapper} to
 * create the Xen host on AWS and then uses {@link XenGuestManagerFactory} to
 * get {@link XenGuestManager}s to provision the Guest (DomU) Virtual Machines.
 * 
 *
 */
public class XenHostManager {
	private static final String S3_DOWNLOAD_MAIN_CLASS = "com.ncc.savior.server.s3.S3Download";
	private static final String XEN_STANDARD = "standard";
	private static final Logger logger = LoggerFactory.getLogger(XenHostManager.class);
	private static final String VM_PREFIX = "VRTU-XG-";

	private ITemplateService templateService;

	@Value("${virtue.aws.persistentStorage.deviceName}")
	private String persistentVolumeDeviceName;

	private AwsEc2Wrapper ec2Wrapper;

	protected IActiveVirtueDao xenVmDao;
	private IKeyManager keyManager;
	private XenGuestManagerFactory xenGuestManagerFactory;
	private Collection<String> securityGroupIds;
	private CompletableFutureServiceProvider serviceProvider;
	private PersistentStorageManager persistentStorageManager;

	private String serverId;
	protected String region;
	protected String bucket;
	protected String kmsKey;
	private IXenVmProvider xenVmProvider;

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper,
			CompletableFutureServiceProvider serviceProvider, Route53Manager route53, IActiveVirtueDao vmDao,
			PersistentStorageManager psm, IVpcSubnetProvider vpcSubnetProvider, ServerIdProvider serverIdProvider,
			ITemplateService templateService, IXenVmProvider xenVmProvider, Collection<String> securityGroupsNames,
			boolean usePublicDns, String region, String imageBucketName, String kmsKey) {
		this.xenVmDao = vmDao;
		this.persistentStorageManager = psm;
		this.serviceProvider = serviceProvider;
		this.ec2Wrapper = ec2Wrapper;
		this.region = region;
		this.bucket = imageBucketName;
		String vpcId = vpcSubnetProvider.getVpcId();
		this.templateService = templateService;
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(securityGroupsNames, vpcId, ec2Wrapper);
		this.serverId = serverIdProvider.getServerId();
		this.keyManager = keyManager;
		this.kmsKey = kmsKey;
		this.xenVmProvider = xenVmProvider;
		this.xenGuestManagerFactory = new XenGuestManagerFactory(keyManager, serviceProvider, route53, templateService);

	}

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper,
			CompletableFutureServiceProvider serviceProvider, Route53Manager route53, IActiveVirtueDao virtueDao,
			PersistentStorageManager psm, IVpcSubnetProvider vpcSubnetProvider, ServerIdProvider serverIdProvider,
			ITemplateService templateService, IXenVmProvider xenVmProvider, String securityGroupsCommaSeparated,
			boolean usePublicDns, String region, String imageBucketName, String kmsKey) {
		this(keyManager, ec2Wrapper, serviceProvider, route53, virtueDao, psm, vpcSubnetProvider, serverIdProvider,
				templateService, xenVmProvider, splitOnComma(securityGroupsCommaSeparated), usePublicDns, region,
				imageBucketName, kmsKey);
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

	// move provision code
	// when xen starts run
	// sudo setupXen.sh
	// sudo nfsd.sh
	// start vms with code below

	public void provisionXenHost(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts,
			CompletableFuture<VirtualMachine> xenFuture, CompletableFuture<Collection<VirtualMachine>> linuxFuture,
			VirtueCreationAdditionalParameters virtueMods) {
		// if caller doesn't provide a future, we may still want one.
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (xenFuture == null) {
			xenFuture = new CompletableFuture<VirtualMachine>();
		}
		CompletableFuture<VirtualMachine> finalXenFuture = xenFuture;
		CompletableFuture<Collection<VirtualMachine>> finalLinuxFuture = linuxFuture;
		String virtueName = virtue.getName();
		virtueName = virtueName.replace(" ", "-");
		// mainly this makes sure the volume is ready
		persistentStorageManager.getOrCreatePersistentStorageForVirtue(virtue.getUsername(), virtue.getTemplateId(),
				virtue.getName());
		Collection<String> secGroupIds = new HashSet<String>(securityGroupIds);
		if (virtueMods.getSecurityGroupId() != null) {
			secGroupIds.add(virtueMods.getSecurityGroupId());
		}
		virtueMods.setVirtueId(virtue.getId());
		virtueMods.setVirtueTemplateId(virtue.getTemplateId());
		virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.USER_VIRTUE);
		virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.XEN_HOST);
		virtueMods.setUsername(virtue.getUsername());
		VirtualMachine xenVm = xenVmProvider.getNewXenVm(virtue, virtueMods, virtueName, secGroupIds);

		// VirtualMachine xenVm = new VirtualMachine(null, null, null, null, OS.LINUX,
		// null,
		// "ec2-34-207-74-33.compute-1.amazonaws.com", 22, "ec2-user", "",
		// "virginiatech_ec2", "");
		// xenVm.setState(VmState.RUNNING);
		//
		xenVm.setId(virtue.getId());
		ArrayList<VirtualMachine> xenVms = new ArrayList<VirtualMachine>();
		xenVms.add(xenVm);

		xenVmDao.updateVms(xenVms);
		CompletableFuture<VirtualMachine> xenProvisionFuture = new CompletableFuture<VirtualMachine>();
		addVmToProvisionPipeline(xenVm, xenProvisionFuture);
		// TODO use future here instead of while loop below
		final String id = virtue.getId();
		for (VirtualMachineTemplate vmt : linuxVmts) {
			String domainUUID = UUID.randomUUID().toString();
			String name = VM_PREFIX + serverId + "-" + virtue.getUsername() + "-" + domainUUID;
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name,
					new ArrayList<ApplicationDefinition>(), VmState.CREATING, vmt.getOs(), "", "", 22, "", "", "", "");
			virtue.getVms().add(vm);
		}

		Runnable provisionRunnable = new Runnable() {

			// private String Dom0NfsSensorCmd =
			// "/home/ec2-user/twosix/matt/nfs-sensor-target/run_docker.sh";

			@Override
			public void run() {
				logger.debug("Starting Host configuration");
				// wait until xen VM is ready
				Optional<VirtualMachine> vmo = xenVmDao.getXenVm(id);
				while (!vmo.isPresent() || !VmState.RUNNING.equals(vmo.get().getState())) {
					JavaUtil.sleepAndLogInterruption(2000);
					vmo = xenVmDao.getXenVm(id);
				}

				VirtualMachine xen = vmo.get();
				ChannelExec channel = null;
				Session session = null;
				String keyName = xenVm.getPrivateKeyName();
				File privateKeyFile = keyManager.getKeyFileByName(keyName);
				try {
					// setup Xen VM
					session = getSession(xen, session, privateKeyFile, 5);
					Session finalSession = session;
					Runnable copyS3Data = getCopyS3DataRunnableUsingJava(linuxVmts, finalSession);
					Thread t = new Thread(copyS3Data, "copy-s3");
					t.start();
					copySshKey(session, privateKeyFile);
					attachPersistentVolume(xen.getInfrastructureId(), virtue.getUsername(), virtue.getTemplateId(),
							virtue.getName());
					waitUntilXlListIsReady(session);
					// JavaUtil.sleepAndLogInterruption(20000);

					HashMap<String, Object> model = new HashMap<String, Object>();
					model.put("xenVm", xenVm);
					SshUtil.runCommandsFromFileWithTimeout(templateService, session, "Dom0-post-config.tpl", model,
							300);

					// SshUtil.sendCommandFromSessionWithTimeout(session,
					// "nohup " + Dom0NfsSensorCmd + " > nfsSensor.log 2>&1", 300);
					// SshUtil.sendCommandFromSession(session, "sudo xl list");
					// List<String> persistOutput = SshUtil.sendCommandFromSession(session,
					// "sudo mkdir -p /persist;sudo mount /dev/nvme1n1 /persist/");
					// logger.debug("mounted persistent volume" + persistOutput);
					logger.debug("Waiting for S3 copy to finish");
					t.join();
					logger.debug("Xen Host configure complete");
					finalXenFuture.complete(xen);
				} catch (JSchException e) {
					logger.trace("Vm is not reachable yet: " + e.getMessage());
				} catch (Exception e) {
					logger.error("error in SSH", e);
				} finally {
					if (channel != null) {
						channel.disconnect();
					}
					if (session != null) {
						session.disconnect();
					}
				}
				// provision Xen Guest VMs
				XenGuestManager guestManager = xenGuestManagerFactory.getXenGuestManager(xenVm);
				logger.debug("starting to provision guests");
				guestManager.provisionGuests(virtue, linuxVmts, finalLinuxFuture, serverId);
			}

			private Runnable getCopyS3DataRunnableUsingJava(Collection<VirtualMachineTemplate> linuxVmts,
					Session finalSession) {
				Runnable copyS3Data = () -> {
					try {
						HashMap<String, Object> model = new HashMap<String, Object>();
						model.put("mainClass", S3_DOWNLOAD_MAIN_CLASS);
						model.put("region", region);
						model.put("kmsKey", kmsKey);
						model.put("bucket", bucket);
						SshUtil.runCommandsFromFile(templateService, finalSession, "Dom0-pre-copy-DomU-image.tpl",
								model);
						for (VirtualMachineTemplate vmt : linuxVmts) {
							model.put("guestVmTemplate", vmt);
							List<String> lines = SshUtil.runCommandsFromFile(templateService, finalSession,
									"Dom0-copy-DomU-image.tpl", model);
							logger.debug("Copy S3 for " + vmt.getName() + "at " + vmt.getTemplatePath() + ": " + lines);
						}
					} catch (TemplateException e) {
						logger.error("Error creating script from template to copy s3 data", e);
					} catch (JSchException e) {
						logger.error("Error attempting to copy s3 data", e);
					} catch (IOException e) {
						logger.error("Error attempting to copy s3 data", e);
					}
				};
				return copyS3Data;
			}

			private Session getSession(VirtualMachine xen, Session session, File privateKeyFile, int maxAttempts) {
				int attempts = 0;
				while (attempts < maxAttempts) {
					try {
						session = SshUtil.getConnectedSession(xen, privateKeyFile);
						break;
					} catch (JSchException e) {
						logger.warn("Connection failed", e);
					}
					JavaUtil.sleepAndLogInterruption(500);
					attempts++;
				}
				return session;
			}
		};

		xenProvisionFuture.handle((xenVm2, ex) -> {
			if (ex == null) {
				provisionRunnable.run();
			} else {
				handleError(virtue, finalXenFuture, xenVm2, ex);
			}
			return xenVm2;
		});
		linuxFuture.handle((myVms, ex) -> {
			if (ex != null) {
				handleError(virtue, finalXenFuture, xenVm, ex);
			}
			return myVms;
		});
		// xenProvisionFuture.thenRun(r);
		// Thread t = new Thread(r, "XenProvisioner-" + id);
		// t.start();
	}

	protected void attachPersistentVolume(String instanceId, String username, String templateId, String templateName) {
		String volumeId = persistentStorageManager.getOrCreatePersistentStorageForVirtue(username, templateId,
				templateName);
		if (volumeId != null) {
			AttachVolumeRequest avr = new AttachVolumeRequest(volumeId, instanceId, persistentVolumeDeviceName);
			AttachVolumeResult avrResult = ec2Wrapper.getEc2().attachVolume(avr);
			String state = avrResult.getAttachment().getState();
			logger.debug("Attaching volume state=" + state);
		}
	}

	private void handleError(VirtueInstance virtue, CompletableFuture<?> future, VirtualMachine xenVm, Throwable ex) {
		Collection<VirtualMachine> vms = virtue.getVms();
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			vm.setState(VmState.ERROR);
			CompletableFuture<VirtualMachine> f = serviceProvider.getVmNotifierService().startFutures(vm, null);
			fc.addFuture(f);
		}
		xenVm.setState(VmState.ERROR);
		CompletableFuture<VirtualMachine> f = serviceProvider.getVmNotifierService().startFutures(xenVm, null);
		fc.addFuture(f);
		// fc.combineFutures(future)
		future.completeExceptionally(ex);
	}

	protected void waitUntilXlListIsReady(Session session) {
		boolean success = false;
		while (!success) {
			try {
				String resp = SshUtil.runCommand(session, "sudo xl list", 5000).getOutput();
				success = resp.contains("Domain-0");
			} catch (IOException | JSchException e) {
				logger.error("error waiting for xl list", e);
			}
			if (!success) {
				JavaUtil.sleepAndLogInterruption(2000);
			}
		}
	}

	protected void copySshKey(Session session, File privateKeyFile) {
		ChannelSftp ch = null;
		try {
			ch = (ChannelSftp) session.openChannel("sftp");
			ch.connect();
			InputStream stream = new FileInputStream(privateKeyFile);
			ch.put(stream, privateKeyFile.getName());
			ch.chmod(0400, privateKeyFile.getName());
		} catch (JSchException | FileNotFoundException | SftpException e) {
			logger.error("Error attempting to copy private key file '" + privateKeyFile + "'", e);
		} finally {
			ch.disconnect();
		}
	}

	public void deleteVirtue(String id, Collection<VirtualMachine> linuxVms,
			CompletableFuture<VirtualMachine> xenFuture, CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (xenFuture == null) {
			xenFuture = new CompletableFuture<VirtualMachine>();
		}
		CompletableFuture<VirtualMachine> finalXenFuture = xenFuture;
		// get XenVmManager for id
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(id);
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();

		if (vmo.isPresent()) {
			VirtualMachine xenVm = vmo.get();
			XenGuestManager guestManager = xenGuestManagerFactory.getXenGuestManager(xenVm);
			// tell XenManager to delete its vms
			linuxFuture.thenRun(() -> {
				vms.add(xenVm);
				ec2Wrapper.deleteVirtualMachines(vms);
				CompletableFuture<VirtualMachine> xenFuture2 = new CompletableFuture<>();
				logger.debug("adding to delete pipeline now!");
				addToDeletePipeline(xenVm, xenFuture2);
				xenFuture2.thenAccept((vm) -> {

					xenVmDao.deleteVm(vm);
					Optional<VirtueInstance> virtue = xenVmDao.getVirtueInstance(id);
					if (virtue.isPresent()) {
						xenVmDao.deleteVirtue(virtue.get());
					}
					for (VirtualMachine guestVm : linuxVms) {
						xenVmDao.deleteVm(guestVm);
					}
					finalXenFuture.complete(vm);
				});
			});
			guestManager.deleteGuests(linuxVms, linuxFuture);
		} else {
			SaviorException e = new SaviorException(SaviorErrorCode.VM_NOT_FOUND,
					"Unable to find Xen VM with id=" + id);
			linuxFuture.completeExceptionally(e);
			xenFuture.completeExceptionally(e);
		}
	}

	public void startVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms,
			CompletableFuture<VirtualMachine> xenFuture, CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		// if caller doesn't provide a future, we may still want one.
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (xenFuture == null) {
			xenFuture = new CompletableFuture<VirtualMachine>();
		}
		CompletableFuture<Collection<VirtualMachine>> finalLinuxFuture = linuxFuture;
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(virtueInstance.getId());
		VirtualMachine xenVm = vmo.get();
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(xenVm);
		for (VirtualMachine vm : linuxVms) {
			CompletableFuture<VirtualMachine> f = serviceProvider.getUpdateStatus().startFutures(vm, VmState.LAUNCHING);
			serviceProvider.getVmNotifierService().chainFutures(f, null);
		}
		ec2Wrapper.startVirtualMachines(vms);
		addVmToStartingPipeline(xenVm, xenFuture);
		// TODO do the following once the xenVm is started
		// XenGuestManager guestManager =
		// xenGuestManagerFactory.getXenGuestManager(xenVm);
		// guestManager.startGuests(linuxVms);

		Runnable startGuestsRunnable = () -> {
			XenGuestManager guestManager = xenGuestManagerFactory.getXenGuestManager(xenVm);
			guestManager.startGuests(linuxVms, finalLinuxFuture);
		};
		xenFuture.thenRun(startGuestsRunnable);
		CompletableFuture<VirtualMachine> finalXenFuture = xenFuture;
		linuxFuture.handle((myVms, ex) -> {
			if (ex != null) {
				handleError(virtueInstance, finalXenFuture, xenVm, ex);
			}
			return myVms;
		});
	}

	public void stopVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms,
			CompletableFuture<VirtualMachine> xenFuture, CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		// if caller doesn't provide a future, we may still want one.
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (xenFuture == null) {
			xenFuture = new CompletableFuture<VirtualMachine>();
		}
		CompletableFuture<VirtualMachine> finalXenFuture = xenFuture;
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(virtueInstance.getId());
		VirtualMachine xenVm = vmo.get();
		XenGuestManager guestManager = xenGuestManagerFactory.getXenGuestManager(xenVm);
		guestManager.stopGuests(linuxVms, linuxFuture);

		Runnable stopHostRunnable = () -> {
			ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
			vms.add(xenVm);
			ec2Wrapper.stopVirtualMachines(vms);
			addVmsToStoppingPipeline(xenVm, finalXenFuture);
		};

		// linuxFuture.thenRun(stopHostRunnable);
		linuxFuture.handle((myVms, ex) -> {
			if (ex != null) {
				handleError(virtueInstance, finalXenFuture, xenVm, ex);
			} else {
				stopHostRunnable.run();
			}
			return myVms;
		});
		finalXenFuture.thenRun(() -> {
			for (VirtualMachine vm : linuxVms) {
				CompletableFuture<VirtualMachine> cf = serviceProvider.getUpdateStatus().startFutures(vm,
						VmState.STOPPED);
				serviceProvider.getVmNotifierService().chainFutures(cf, null);
			}
		});
	}

	private void addVmToProvisionPipeline(VirtualMachine xenVm, CompletableFuture<VirtualMachine> xenFuture) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(xenVm, v);
		cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
		cf = serviceProvider.getEnsureDeleteVolumeOnTermination().chainFutures(cf, v);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		// cf = serviceProvider.getErrorCausingService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
		cf = serviceProvider.getAddRsa().chainFutures(cf, v);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
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

	private void addToDeletePipeline(VirtualMachine xenVm, CompletableFuture<VirtualMachine> xenFuture) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getUpdateStatus().startFutures(xenVm, VmState.DELETING);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, false);
		cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.DELETED);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf.thenAccept((vm) -> {
			xenFuture.complete(vm);
		});
	}

	private void addVmToStartingPipeline(VirtualMachine xenVm, CompletableFuture<VirtualMachine> xenFuture) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(xenVm, v);
		cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf.thenAccept((VirtualMachine vm) -> {
			logger.debug("xen host starting future complete");
			xenFuture.complete(vm);
		});
	}

	private void addVmsToStoppingPipeline(VirtualMachine xenVm, CompletableFuture<VirtualMachine> xenFuture) {
		Void v = null;
		CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(xenVm, false);
		cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.STOPPED);
		cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf.thenAccept((VirtualMachine vm) -> {
			logger.debug("xen host stopping future complete");
			xenFuture.complete(vm);
		});
	}

	public XenGuestManager getGuestManager(String virtueId) {
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(virtueId);
		VirtualMachine xenVm = vmo.get();
		return xenGuestManagerFactory.getXenGuestManager(xenVm);
	}

}
