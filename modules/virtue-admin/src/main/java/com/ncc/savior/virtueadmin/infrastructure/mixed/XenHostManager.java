package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.InstanceType;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.FutureCombiner;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * This class handles creation, deletion, start, and stop among other management
 * task of Xen Host Virtual Machines (Dom0). It uses {@link AwsEc2Wrapper} to
 * create the Xen host on AWS and then uses {@link XenGuestManagerFactory} to
 * get {@link XenGuestManager}s to provision the Guest (DomU) Virtual Machines.
 * 
 *
 */
public class XenHostManager {
	private static final String VM_PREFIX = "VRTU-XG-";
	private static final Logger logger = LoggerFactory.getLogger(XenHostManager.class);
	private VirtualMachineTemplate xenVmTemplate;
	private AwsEc2Wrapper ec2Wrapper;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	protected IActiveVirtueDao xenVmDao;
	private String serverUser;
	private IKeyManager keyManager;
	private XenGuestManagerFactory xenGuestManagerFactory;
	private String subnetId;
	private Collection<String> securityGroupIds;
	private CompletableFutureServiceProvider serviceProvider;

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper,
			CompletableFutureServiceProvider serviceProvider, Route53Manager route53, IActiveVirtueDao vmDao,
			Collection<String> securityGroupsNames, String subnetName, String xenAmi, String xenLoginUser,
			String xenKeyName, InstanceType xenInstanceType, boolean usePublicDns) {
		this.xenVmDao = vmDao;
		this.serviceProvider = serviceProvider;
		this.ec2Wrapper = ec2Wrapper;
		this.subnetId = AwsUtil.getSubnetIdFromName(subnetName, ec2Wrapper);
		String vpcId = AwsUtil.getVpcIdFromSubnetId(subnetId, ec2Wrapper);
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(securityGroupsNames, vpcId, ec2Wrapper);
		this.xenKeyName = xenKeyName;
		this.xenInstanceType = xenInstanceType;
		this.serverUser = System.getProperty("user.name");
		this.keyManager = keyManager;
		this.xenGuestManagerFactory = new XenGuestManagerFactory(keyManager, serviceProvider, route53);
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX, xenAmi,
				new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), "system");
	}

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper,
			CompletableFutureServiceProvider serviceProvider, Route53Manager route53, IActiveVirtueDao virtueDao,
			String securityGroupsCommaSeparated, String subnetId, String xenAmi, String xenUser, String xenKeyName,
			String xenInstanceType, boolean usePublicDns) {
		this(keyManager, ec2Wrapper, serviceProvider, route53, virtueDao, splitOnComma(securityGroupsCommaSeparated),
				subnetId, xenAmi, xenUser, xenKeyName, InstanceType.fromValue(xenInstanceType), usePublicDns);
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
			CompletableFuture<VirtualMachine> xenFuture, CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		// if caller doesn't provide a future, we may still want one.
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (xenFuture == null) {
			xenFuture = new CompletableFuture<VirtualMachine>();
		}
		CompletableFuture<VirtualMachine> finalXenFuture = xenFuture;
		CompletableFuture<Collection<VirtualMachine>> finalLinuxFuture = linuxFuture;
		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate,
				"VRTU-Xen-" + serverUser + "-" + virtue.getUsername() + "-", securityGroupIds, xenKeyName,
				xenInstanceType,
				subnetId);

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
			String name = VM_PREFIX + serverUser + "-" + virtue.getUsername() + "-" + domainUUID;
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name,
					new ArrayList<ApplicationDefinition>(), VmState.CREATING, vmt.getOs(), "", "", 22, "", "", "", "");
			virtue.getVms().add(vm);
		}

		Runnable r = new Runnable() {

			private String Dom0NfsSensorCmd = "/home/ec2-user/twosix/matt/nfs-sensor-target/run_docker.sh";

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

					copySshKey(session, privateKeyFile);
					waitUntilXlListIsReady(session);
					// JavaUtil.sleepAndLogInterruption(20000);
					SshUtil.sendCommandFromSessionWithTimeout(session,
							"nohup " + Dom0NfsSensorCmd + " > nfsSensor.log 2>&1", 300);
					SshUtil.sendCommandFromSession(session, "sudo xl list");
					logger.trace("Xen Host configure complete");
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
				guestManager.provisionGuests(virtue, linuxVmts, finalLinuxFuture, serverUser);
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
				r.run();
			} else {
				handleError(virtue, finalXenFuture, xenVm2, ex);
			}
			return xenVm2;
		});
		// xenProvisionFuture.thenRun(r);
		// Thread t = new Thread(r, "XenProvisioner-" + id);
		// t.start();
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
				List<String> resp = SshUtil.sendCommandFromSession(session, "sudo xl list");
				for (String line : resp) {
					if (line.contains("Domain-0")) {
						success = true;
					}
				}
			} catch (IOException | JSchException e) {
				logger.error("error waiting for xl list", e);
			}
			JavaUtil.sleepAndLogInterruption(2000);
		}
	}

	protected void waitUntilXlListIsReady(PrintStream ps, BufferedReader br) {
		while (true) {
			ps.println("sudo xl list");

		}

	}

	protected void copySshKey(Session session, File privateKeyFile) {
		ChannelSftp ch = null;
		try {
			ch = (ChannelSftp) session.openChannel("sftp");
			ch.connect();
			InputStream stream = new FileInputStream(privateKeyFile);
			ch.put(stream, privateKeyFile.getName());
			// 400 from octal to decimal
			ch.chmod(256, privateKeyFile.getName());
		} catch (JSchException e) {
			logger.error("Error attempting to copy private key", e);
		} catch (FileNotFoundException e) {
			logger.error("Error attempting to copy private key", e);
		} catch (SftpException e) {
			logger.error("Error attempting to copy private key", e);
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
			guestManager.deleteGuests(linuxVms, linuxFuture);
			linuxFuture.thenRun(() -> {
				vms.add(xenVm);
				ec2Wrapper.deleteVirtualMachines(vms);
				CompletableFuture<VirtualMachine> xenFuture2 = new CompletableFuture<>();
				addToDeletePipeline(xenVm, xenFuture2);
				xenFuture2.thenAccept((vm) -> {
					xenVmDao.deleteVm(vm);
					finalXenFuture.complete(vm);
				});
			});
		} else {
			SaviorException e = new SaviorException(SaviorException.UNKNOWN_ERROR,
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

		linuxFuture.thenRun(stopHostRunnable);
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
		CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(xenVm, false);
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

	public void setServerUser(String serverUser) {
		if (serverUser != null && !serverUser.trim().equals("")) {
			this.serverUser = serverUser;
		}
	}

}
