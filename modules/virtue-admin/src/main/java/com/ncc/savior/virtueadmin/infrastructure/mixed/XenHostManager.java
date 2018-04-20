package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.InstanceType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class XenHostManager {
	private static final Logger logger = LoggerFactory.getLogger(XenHostManager.class);
	private VirtualMachineTemplate xenVmTemplate;
	private IUpdateListener<VirtualMachine> notifier;
	private AwsEc2Wrapper ec2Wrapper;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private XenHostVmUpdater updater;
	protected IActiveVirtueDao xenVmDao;
	private String serverUser;
	private IKeyManager keyManager;
	private XenGuestManagerFactory xenGuestManagerFactory;
	private String subnetId;
	private Collection<String> securityGroupIds;
	private static final String VM_PREFIX = "VRTU-";

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> actualVmNotifier, Route53Manager route53,
			Collection<String> securityGroupsNames, String subnetName, String xenAmi, String xenLoginUser,
			String xenKeyName, InstanceType xenInstanceType, boolean usePublicDns) {
		this.notifier = actualVmNotifier;
		this.ec2Wrapper = ec2Wrapper;
		this.subnetId = AwsUtil.getSubnetIdFromName(subnetName, ec2Wrapper);
		String vpcId = AwsUtil.getVpcIdFromSubnetId(subnetId, ec2Wrapper);
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(securityGroupsNames, vpcId, ec2Wrapper);
		this.xenKeyName = xenKeyName;
		this.xenInstanceType = xenInstanceType;
		this.xenVmDao = xenVmDao;
		this.serverUser = System.getProperty("user.name");
		this.keyManager = keyManager;
		this.xenGuestManagerFactory = new XenGuestManagerFactory(keyManager, notifier, route53);
		IUpdateListener<VirtualMachine> xenListener = new IUpdateListener<VirtualMachine>() {
			@Override
			public void updateElements(Collection<VirtualMachine> elements) {
				xenVmDao.updateVms(elements);
			}
		};
		this.updater = new XenHostVmUpdater(ec2Wrapper.getEc2(), xenListener, keyManager, usePublicDns);
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX, xenAmi,
				new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), "system");
	}

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> notifier, Route53Manager route53, String securityGroupsCommaSeparated,
			String subnetId, String xenAmi, String xenUser, String xenKeyName, String xenInstanceType,
			boolean usePublicDns) {
		this(keyManager, ec2Wrapper, xenVmDao, notifier, route53, splitOnComma(securityGroupsCommaSeparated), subnetId,
				xenAmi, xenUser, xenKeyName, InstanceType.fromValue(xenInstanceType), usePublicDns);
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

	public void provisionXenHost(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts) {
		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate,
				"Xen-" + serverUser + "-" + virtue.getUsername() + "-", securityGroupIds, xenKeyName, xenInstanceType,
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
		updater.addVmToProvisionPipeline(xenVms);
		final String id = virtue.getId();
		for (VirtualMachineTemplate vmt : linuxVmts) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), "",
					new ArrayList<ApplicationDefinition>(), VmState.CREATING, vmt.getOs(), "", "", 22, "", "", "", "");
			virtue.getVms().add(vm);
		}

		Runnable r = new Runnable() {

			private String Dom0NfsSensorCmd = "/home/ec2-user/twosix/matt/nfs-sensor-target/run_docker.sh";

			@Override
			public void run() {
				// wait until xen VM is ready
				Optional<VirtualMachine> vmo = xenVmDao.getXenVm(id);
				while (!vmo.isPresent() || !VmState.RUNNING.equals(vmo.get().getState())) {
					JavaUtil.sleepAndLogInterruption(2000);
					vmo = xenVmDao.getXenVm(id);
				}

				VirtualMachine xen = vmo.get();
				JSch ssh = new JSch();
				ChannelExec channel = null;
				Session session = null;
				String keyName = xenVm.getPrivateKeyName();
				File privateKeyFile = keyManager.getKeyFileByName(keyName);
				try {
					// setup Xen VM
					ssh.addIdentity(privateKeyFile.getAbsolutePath());
					session = ssh.getSession(xen.getUserName(), xen.getHostname(), 22);
					session.setConfig("PreferredAuthentications", "publickey");
					session.setConfig("StrictHostKeyChecking", "no");
					session.setTimeout(500);
					session.connect();

					copySshKey(session, privateKeyFile);

					Channel myChannel = session.openChannel("shell");
					OutputStream ops = myChannel.getOutputStream();
					PrintStream ps = new PrintStream(ops, true);

					myChannel.connect();
					InputStream input = myChannel.getInputStream();
					InputStreamReader reader = new InputStreamReader(input);
					BufferedReader br = new BufferedReader(reader);
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							String line;
							try {
								while ((line = br.readLine()) != null) {
									logger.debug(line);
									if (line.contains("finished setting up Xen " + id) && !line.contains("echo")) {
										break;
									}
								}
							} catch (IOException e) {
								logger.error("Error reading SSH output", e);
							}
						}
					});
					t.start();
					// commands
					// ps.println("sudo ./setupXen.sh");
					// ps.println("sudo ./nfsd.sh &");
					// TODO this sleep is here
					JavaUtil.sleepAndLogInterruption(10000);
					// ps.println("\035");
					ps.println("nohup " + Dom0NfsSensorCmd + " > nfsSensor.log 2>&1");
					ps.println("sudo xl list");
					ps.println("echo finished setting up Xen " + id);
					t.join(20000);
					JavaUtil.sleepAndLogInterruption(1000);
					ps.println("exit");
					JavaUtil.sleepAndLogInterruption(1000);
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
				guestManager.provisionGuests(virtue, linuxVmts);
			}
		};

		Thread t = new Thread(r, "XenProvisioner-" + id);
		t.start();
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

	public void deleteVirtue(String id, Collection<VirtualMachine> linuxVms) {
		// get XenVmManager for id
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(id);
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		if (vmo.isPresent()) {
			VirtualMachine xenVm = vmo.get();
			XenGuestManager guestManager = xenGuestManagerFactory.getXenGuestManager(xenVm);
			// tell XenManager to delete its vms
			guestManager.deleteGuests(linuxVms);
			// TODO schedule once Vm's are deleted, XenManager will delete itself.
			vms.add(xenVm);
			ec2Wrapper.deleteVirtualMachines(vms);
		}
	}

	public void startVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(virtueInstance.getId());
		VirtualMachine xenVm = vmo.get();
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(xenVm);
		ec2Wrapper.startVirtualMachines(vms);
		updater.addVmsToStartingPipeline(vms);
		// TODO do the following once the xenVm is started
		// XenGuestManager guestManager =
		// xenGuestManagerFactory.getXenGuestManager(xenVm);
		// guestManager.startGuests(linuxVms);
	}

	public void stopVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		Optional<VirtualMachine> vmo = xenVmDao.getXenVm(virtueInstance.getId());
		VirtualMachine xenVm = vmo.get();
		XenGuestManager guestManager = xenGuestManagerFactory.getXenGuestManager(xenVm);
		guestManager.stopGuests(linuxVms);
		// TODO wait until stop is done

		// ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		// vms.add(xenVm);
		// ec2Wrapper.stopVirtualMachines(vms);
		// updater.addVmsToStoppingPipeline(vms);

	}

	protected void setServerUser(String serverUser) {
		if (serverUser != null && !serverUser.trim().equals("")) {
			this.serverUser = serverUser;
		}
	}

}
