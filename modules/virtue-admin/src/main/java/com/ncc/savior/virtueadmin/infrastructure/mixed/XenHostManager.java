package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.InstanceType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class XenHostManager {
	private static final Logger logger = LoggerFactory.getLogger(XenAwsMixCloudManager.class);
	private VirtualMachineTemplate xenVmTemplate;
	private IUpdateListener<VirtualMachine> notifier;
	private AwsEc2Wrapper ec2Wrapper;
	private Collection<String> securityGroups;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private XenHostVmUpdater updater;
	protected IActiveVirtueDao vmDao;
	private String serverUser;
	private IKeyManager keyManager;
	private static final String VM_PREFIX = "VRTU-";

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> actualVmNotifier, Collection<String> securityGroups, String xenKeyName,
			InstanceType xenInstanceType) {
		this.notifier = actualVmNotifier;
		this.ec2Wrapper = ec2Wrapper;
		this.securityGroups = securityGroups;
		this.xenKeyName = xenKeyName;
		this.xenInstanceType = xenInstanceType;
		this.vmDao = xenVmDao;
		this.serverUser = System.getProperty("user.name");
		this.keyManager = keyManager;
		IUpdateListener<VirtualMachine> xenListener = new IUpdateListener<VirtualMachine>() {
			@Override
			public void updateElements(Collection<VirtualMachine> elements) {
				xenVmDao.updateVms(elements);
			}
		};
		this.updater = new XenHostVmUpdater(ec2Wrapper.getEc2(), xenListener, keyManager);
		String templatePath = "ami-e156839c";
		String xenLoginUser = "ec2-user";
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX,
				templatePath, new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), "system");
	}

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> notifier, String securityGroupsCommaSeparated, String xenKeyName,
			String xenInstanceType) {
		this(keyManager, ec2Wrapper, xenVmDao, notifier, splitOnComma(securityGroupsCommaSeparated), xenKeyName,
				InstanceType.fromValue(xenInstanceType));
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
				"Xen-" + serverUser + "-" + virtue.getUsername() + "-", securityGroups, xenKeyName, xenInstanceType);
		
		// VirtualMachine xenVm = new VirtualMachine(null, null, null, null, OS.LINUX,
		// null,
		// "ec2-34-229-112-147.compute-1.amazonaws.com", 22, "ec2_user", "",
		// "virginiatech_ec2", "");
		// xenVm.setState(VmState.RUNNING);
		//
		xenVm.setId(virtue.getId());
		ArrayList<VirtualMachine> xenVms = new ArrayList<VirtualMachine>();
		xenVms.add(xenVm);

		vmDao.updateVms(xenVms);
		updater.addVmToProvisionPipeline(xenVms);
		final String id = virtue.getId();
		for (VirtualMachineTemplate vmt : linuxVmts) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), "",
					new ArrayList<ApplicationDefinition>(), VmState.CREATING, vmt.getOs(), "", "", 22, "", "", "", "");

			virtue.getVms().add(vm);

		}

		Runnable r = new Runnable() {

			@Override
			public void run() {
				// wait until xen VM is ready
				Optional<VirtualMachine> vmo = vmDao.getXenVm(id);
				while (!vmo.isPresent() || !VmState.RUNNING.equals(vmo.get().getState())) {
					JavaUtil.sleepAndLogInterruption(2000);
					vmo = vmDao.getXenVm(id);
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

					Channel myChannel = session.openChannel("shell");
					OutputStream ops = myChannel.getOutputStream();
					PrintStream ps = new PrintStream(ops, true);

					myChannel.connect();
					InputStream input = myChannel.getInputStream();
					InputStreamReader reader = new InputStreamReader(input);
					BufferedReader br = new BufferedReader(reader);
					Runnable readerRunnable = new Runnable() {

						@Override
						public void run() {

							boolean stopReaderThread = false;

							String line;
							long start = System.currentTimeMillis();
							try {
								while (!stopReaderThread && (line = br.readLine()) != null) {
									System.out.println(line);
									if (System.currentTimeMillis() - start > 15000) {
										break;
									}
								}
							} catch (IOException e) {

							}
						}
					};
					Thread t = new Thread(readerRunnable);
					t.start();
					// commands
					ps.println("sudo ./setupXen.sh");
					ps.println("sudo ./nfsd.sh &");
					JavaUtil.sleepAndLogInterruption(3000);
					ps.println("sudo xl list");
					t.join(15000);
					// ps.print("\035");

					// provision Xen Guest VMs

					Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>(virtue.getVms());
					Iterator<VirtualMachine> vmsItr = vms.iterator();

					for (VirtualMachineTemplate vmt : linuxVmts) {
						VirtualMachine vm = vmsItr.next();
						String ipAddress = "0.0.0.0";
						String clientUser = virtue.getUsername();
						String domainUUID = UUID.randomUUID().toString();
						String name = VM_PREFIX + clientUser + "-" + virtue.getUsername() + "-" + domainUUID;
						String loginUsername = vmt.getLoginUser();

						// try {
						// session = ssh.getSession(xen.getUserName(), xen.getHostname(), 22);
						// session.setConfig("PreferredAuthentications", "publickey");
						// session.setConfig("StrictHostKeyChecking", "no");
						// session.setTimeout(500);
						// session.connect();

						// myChannel = session.openChannel("shell");
						// ops = myChannel.getOutputStream();
						// ps = new PrintStream(ops, true);
						//
						// myChannel.connect();
						// input = myChannel.getInputStream();

						// commands
						ps.println("sudo xl console");
						ps.println("cd ./app-domains");
						ps.println("./create.sh " + name);
						ps.println("sudo xl console " + name);

						// ps.println("exit");
						ipAddress = getIpAddress(br, myChannel);
						new Thread(readerRunnable).start();
						int bracket = 29;
						String c = "\\u" + bracket;
						ps.print(c);
						ps.print("\029");
						ps.print("\035");
						ps.println("sudo xl list ");
						String dnsAddress = ""; // we don't have dns name yet.
						vm.setName(name);
						vm.setInfrastructureId(name);
						vm.setUserName(loginUsername);
						vm.setHostname(dnsAddress);
						vm.setIpAddress(ipAddress);
						vm.setState(VmState.RUNNING);
						JavaUtil.sleepAndLogInterruption(5000);
					}
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
					// JavaUtil.closeIgnoreErrors(reader, ereader);
				}

				notifier.updateElements(virtue.getVms());

				logger.info("Created vms " + virtue.getVms());
			}
		};

		Thread t = new Thread(r, "XenProvisioner-" + id);
		t.start();
	}

	public void deleteVirtue(String id, Collection<VirtualMachine> linuxVms) {
		// TODO get XenVmManager for id
		// TODO tell XenManager to delete its vms
		// TODO schedule once Vm's are deleted, XenManager will delete itself.
		Optional<VirtualMachine> vm = vmDao.getXenVm(id);
		if (vm.isPresent()) {
			ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
			vms.add(vm.get());
			ec2Wrapper.deleteVirtualMachines(vms);
		}

	}

	public void startVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	public void stopVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param input
	 * @param channel
	 */
	private static String getIpAddress(BufferedReader reader, Channel channel) throws Exception {
		String virtue_ip = "0.0.0.0";
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println((line));
			if (line.contains("virtue-ip")) {
				virtue_ip = findIP(line);
				return virtue_ip;
			}
		}

		return virtue_ip;
	}

	private static String findIP(String substring) {
		String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(substring);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return "0.0.0.0";
		}

	}

	protected void setServerUser(String serverUser) {
		if (serverUser != null && !serverUser.trim().equals("")) {
			this.serverUser = serverUser;
		}
	}

}
