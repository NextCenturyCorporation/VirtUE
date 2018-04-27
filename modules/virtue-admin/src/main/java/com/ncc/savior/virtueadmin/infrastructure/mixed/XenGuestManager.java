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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.virtueadmin.infrastructure.aws.FutureCombiner;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;
import com.ncc.savior.virtueadmin.util.SshUtil;

/**
 * Handles creation, starting, stopping, deleting and some other management of
 * XenGuest (DomU) Virtual Machines. One instance of this class maps directly to
 * only one instance of Xen.
 * 
 *
 */
public class XenGuestManager {
	private static final Logger logger = LoggerFactory.getLogger(XenGuestManager.class);
	private static final String VM_PREFIX = "VRTU-";
	private static final String PORTS_FILE = "ports.properties";
	private static final String SENSOR_SCRIPT = "run_sensors.sh";
	private File keyFile;
	private VirtualMachine xenVm;
	private Route53Manager route53;
	private CompletableFutureServiceProvider serviceProvider;

	public XenGuestManager(VirtualMachine xenVm, File keyFile, CompletableFutureServiceProvider serviceProvider,
			Route53Manager route53) {
		this.keyFile = keyFile;
		this.xenVm = xenVm;
		this.serviceProvider = serviceProvider;
		this.route53 = route53;
	}

	public void provisionGuests(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts,
			CompletableFuture<Collection<VirtualMachine>> xenGuestFuture) {

		ChannelExec channel = null;
		Session session = null;
		logger.debug("Provisioning linux guests=" + linuxVmts);
		Collection<VirtualMachine> linuxVms = new ArrayList<VirtualMachine>();
		try {
			session = SshUtil.getConnectedSession(xenVm, keyFile);

			Collection<VirtualMachine> vms = virtue.getVms();
			Iterator<VirtualMachine> vmsItr = vms.iterator();
			int externalSshPort = 8001;
			int externalSensingPort = 12001;
			int startingInternalPort = 11001;
			int numSensingPorts = 3;
			// String sshConfig = sendCommandFromSession(session, "cat
			// ~/.ssh/config").toString();
			// if (!sshConfig.contains("UserKnownHostsFile")) {
			// sendCommandFromSession(session, "echo \"UserKnownHostsFile=/dev/null\" >>
			// ~/.ssh/config");
			// }
			// sendCommandFromSession(session, "echo \"StrictHostKeyChecking=no\" >>
			// ~/.ssh/config");
			// sendCommandFromSession(session, "chmod 744 ~/.ssh/config");

			for (VirtualMachineTemplate vmt : linuxVmts) {

				// TODO this iterator filtering os seems weird.
				VirtualMachine vm = vmsItr.next();
				while (vm.getOs().equals(OS.WINDOWS)) {
					logger.debug("Skipping provision of windows vm=" + vm);
					vm = vmsItr.next();
				}
				linuxVms.add(vm);
				logger.debug("Starting provision of guest=" + vm);
				String ipAddress = "0.0.0.0";
				String clientUser = virtue.getUsername();
				String domainUUID = UUID.randomUUID().toString();
				String name = VM_PREFIX + clientUser + "-" + virtue.getUsername() + "-" + domainUUID;

				// name = "VRTU-test";
				// ipAddress = "192.168.0.54";
				String loginUsername = "user";
				// roles: default, email, power, god
				String role = vmt.getSecurityTag();
				// String loginUsername = vmt.getLoginUser();
				createGuestVm(session, name, role);
				ipAddress = getGuestVmIpAddress(session, name);
				List<String> sshConfig = SshUtil.sendCommandFromSession(session, "cat ~/.ssh/known_hosts");
				boolean hostIsKnown = false;
				for (String line : sshConfig) {
					if (line.contains(ipAddress)) {
						hostIsKnown = true;
						break;
					}
				}
				if (!hostIsKnown) {
					String hostCmd = "ssh-keyscan " + ipAddress + " >> ~/.ssh/known_hosts";
					SshUtil.sendCommandFromSession(session, hostCmd);
				}

				logger.debug("Attempting to setup port forwarding. ");
				String hostname = SshUtil.sendCommandFromSession(session, "hostname").get(0);
				String dns = route53.AddARecord(hostname, xenVm.getInternalIpAddress());
				setupHostname(session, loginUsername, hostname, dns, ipAddress);
				setupPortForwarding(session, externalSensingPort, startingInternalPort, numSensingPorts, ipAddress);

				externalSensingPort += numSensingPorts;
				catFile(session, ipAddress, loginUsername, PORTS_FILE);
				startSensors(session, SENSOR_SCRIPT, ipAddress, loginUsername);

				String dnsAddress = ""; // we don't have dns name yet.
				vm.setName(name);
				vm.setInfrastructureId(name);
				vm.setUserName(loginUsername);
				vm.setInternalHostname(hostname);
				vm.setHostname(dnsAddress);
				vm.setIpAddress(ipAddress);
				int internalPort = 22;
				String cmd = String.format(
						"sudo iptables -t nat -A PREROUTING -p tcp -i eth0  --dport %d -j DNAT --to-destination %s:%d ;",
						externalSshPort, ipAddress, internalPort);
				cmd += String.format(
						"sudo iptables -A FORWARD -p tcp -d %s --dport %d  -m state --state NEW,ESTABLISHED,RELATED -j ACCEPT",
						ipAddress, internalPort);
				// cmd += ";sudo ip route";
				SshUtil.sendCommandFromSession(session, cmd);
				SshUtil.sendCommandFromSession(session, "sudo iptables -vnL -t nat\n");
				JavaUtil.sleepAndLogInterruption(1000);
				vm.setState(VmState.LAUNCHING);
				vm.setHostname(xenVm.getHostname());
				vm.setIpAddress(xenVm.getIpAddress());
				vm.setInternalHostname(dns);
				vm.setInternalIpAddress(ipAddress);
				vm.setPrivateKeyName(xenVm.getPrivateKeyName());
				vm.setSshPort(externalSshPort);
				vm.setApplications(new ArrayList<ApplicationDefinition>(vmt.getApplications()));
				externalSshPort++;
				logger.debug("finished provisioning linux guest Vm=" + vm.getName());
			}
			logger.debug("finished provisioning of linux guest VMs=" + linuxVmts);
			addVmToProvisionPipeline(vms, xenGuestFuture);
		} catch (JSchException e) {
			logger.error("Vm is not reachable yet: " + e.getMessage());
			xenGuestFuture.completeExceptionally(e);
		} catch (Exception e) {
			logger.error("error in SSH", e);
			xenGuestFuture.completeExceptionally(e);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}

	private void addVmToProvisionPipeline(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(vm, true);
			cf = serviceProvider.getAddRsa().chainFutures(cf, v);
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(linuxFuture);
	}

	private void startSensors(Session session, String sensorScript, String ipAddress, String username)
			throws JSchException, IOException {
		String cmd = "ssh -i virginiatech_ec2.pem " + username + "@" + ipAddress + " \" nohup sudo ./" + sensorScript
				+ " > sensing.log 2>&1 \"";
		List<String> output = SshUtil.sendCommandFromSession(session, cmd);
	}

	private String getGuestVmIpAddress(Session session, String name) throws JSchException, IOException {
		String ipAddress;
		logger.debug("Attempting to get IP address of guest.");
		CommandHandler ch = getCommandHandlerFromSession(session);
		ipAddress = getIpFromConsole(ch, name);
		return ipAddress;
	}

	private void createGuestVm(Session session, String name, String role) throws JSchException, IOException {
		String command = "cd ./app-domains; sudo ./create.sh " + name;
		if (JavaUtil.isNotEmpty(role)) {
			command += " " + role;
		}
		SshUtil.sendCommandFromSession(session, command);
	}

	private String createGuestVm2(Session session, String name, String role) throws JSchException, IOException {
		CommandHandler ch = getCommandHandlerFromSession(session);
		String finishString = "finished with " + name;
		logger.debug("Sending commands to create VM.");
		String createCmd = "sudo ./create.sh " + name;
		if (JavaUtil.isNotEmpty(role)) {
			createCmd += " " + role;
		}
		logger.debug("CreateCmd=" + createCmd);
		ch.sendln("sudo xl list");
		ch.sendln("cd ./app-domains");
		ch.sendln(createCmd);
		ch.sendln("cd ..");
		ch.sendln("echo " + finishString);
		ch.readUtil(finishString, "echo");
		try {
			ch.close();
		} catch (IOException e) {
			logger.error("error closing connection when creating guest VM", e);
		}
		return finishString;
	}

	private CommandHandler getCommandHandlerFromSession(Session session) throws JSchException, IOException {
		Channel myChannel = session.openChannel("shell");
		OutputStream ops = myChannel.getOutputStream();
		PrintStream ps = new PrintStream(ops, true);
		myChannel.connect();
		InputStream input = myChannel.getInputStream();
		InputStreamReader reader = new InputStreamReader(input);
		BufferedReader br = new BufferedReader(reader);

		CommandHandler ch = new CommandHandler(ps, br, myChannel);
		return ch;
	}

	private void setupHostname(Session session, String username, String hostname, String dns, String ipAddress)
			throws JSchException, IOException {
		// hostname = hostname`";
		String cmd = "ssh -i virginiatech_ec2.pem " + username + "@" + ipAddress + " \"echo hostname=" + hostname
				+ " > " + PORTS_FILE + "\"";
		String cmd2 = "ssh -i virginiatech_ec2.pem " + username + "@" + ipAddress + " \"echo dns=" + dns + " >> "
				+ PORTS_FILE + "\"";
		SshUtil.sendCommandFromSession(session, cmd);
		SshUtil.sendCommandFromSession(session, cmd2);
	}

	private void setupPortForwarding(Session session, int externalSensingPort, int startingInternalPort,
			int numSensingPorts, String ipAddress) throws JSchException, IOException, SftpException {
		String script = "./pf.sh";
		ChannelSftp ch = (ChannelSftp) session.openChannel("sftp");
		ch.connect();
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("portForwarding.sh");
		ch.put(stream, script + "win");

		ch.disconnect();
		JavaUtil.sleepAndLogInterruption(100);

		JavaUtil.sleepAndLogInterruption(100);
		SshUtil.sendCommandFromSession(session, "tr -d '\\15\\32' < " + script + "win > " + script);
		SshUtil.sendCommandFromSession(session, "chmod 755 " + script);
		JavaUtil.sleepAndLogInterruption(100);
		for (int i = 0; i < numSensingPorts; i++) {

			String cmd = script + " " + (i + externalSensingPort) + " " + ipAddress + " " + (startingInternalPort + i);
			SshUtil.sendCommandFromSession(session, cmd);
			JavaUtil.sleepAndLogInterruption(100);
		}
		JavaUtil.sleepAndLogInterruption(100);
	}

	// private String getIpFromScript(CommandHandler ch, String name) {
	// JavaUtil.sleepAndLogInterruption(100);
	// ch.sendln("getip/findip.py " + name);
	// JavaUtil.sleepAndLogInterruption(300);
	// List<String> lines = ch.getAll();
	// for (String line : lines) {
	// logger.debug("Find Ip result: " + line);
	// String ip = line;
	// if (ip == null || ip.contains("Error") || ip.trim().equals("") ||
	// ip.contains("No such file")
	// || !ip.contains(".")) {
	// continue;
	// } else {
	// return null;
	// }
	// }
	// return null;
	// }

	private void catFile(Session session, String ipAddress, String username, String file)
			throws JSchException, IOException {
		String cmd = "ssh -i virginiatech_ec2.pem " + username + "@" + ipAddress + " \"cat " + file + "\"";
		SshUtil.sendCommandFromSession(session, cmd);
	}

	private String getIpFromConsole(CommandHandler ch, String name) {
		String ipAddress;
		ch.sendln("sudo xl console " + name);
		JavaUtil.sleepAndLogInterruption(200);
		ipAddress = getIpAddress(ch);
		JavaUtil.sleepAndLogInterruption(2000);
		ch.sendln("");
		ch.sendln("\035");
		ch.sendln("");
		return ipAddress;
	}

	/**
	 * @param input
	 * @param channel
	 */
	public static String getIpAddress(CommandHandler ch) {
		String virtue_ip = "0.0.0.0";
		String line = null;
		while ((line = ch.readLine()) != null) {
			// System.out.println((line));
			if (line.contains("virtue-ip")) {
				virtue_ip = findIP(line);
				return virtue_ip;
			}
		}
		return virtue_ip;
	}

	public static String findIP(String substring) {
		String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(substring);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return "0.0.0.0";
		}
	}

	public void startGuests(Collection<VirtualMachine> linuxVms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Session session = null;
		try {
			session = SshUtil.getConnectedSession(xenVm, keyFile);
			for (VirtualMachine vm : linuxVms) {
				createGuestVm(session, vm.getName(), null);
			}
			addToStartPipeline(linuxVms, linuxFuture);
		} catch (JSchException | IOException e) {
			linuxFuture.completeExceptionally(e);
		} finally {
			SshUtil.disconnectLogErrors(session);
		}
	}

	private void addToStartPipeline(Collection<VirtualMachine> linuxVms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : linuxVms) {
			CompletableFuture<VirtualMachine> cf = null;
			// = serviceProvider.getAwsRenamingService().startFutures(vm, v);
			//
			logger.error("NEED TO FIGURE OUT STARTING PIPELINE");
			// cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
			cf = serviceProvider.getUpdateStatus().startFutures(vm, VmState.LAUNCHING);
			cf = serviceProvider.getNetworkSettingService().chainFutures(cf, xenVm);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(linuxFuture);
	}

	public void stopGuests(Collection<VirtualMachine> linuxVms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Session session = null;
		try {
			session = SshUtil.getConnectedSession(xenVm, keyFile);
			for (VirtualMachine vm : linuxVms) {
				SshUtil.sendCommandFromSession(session, "sudo xl shutdown " + vm.getName());
			}
			addToStopPipeline(linuxVms, linuxFuture);
		} catch (JSchException | IOException e) {
			linuxFuture.completeExceptionally(e);
		} finally {
			SshUtil.disconnectLogErrors(session);
		}
	}

	private void addToStopPipeline(Collection<VirtualMachine> linuxVms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Void v = null;
		FutureCombiner<VirtualMachine> fc = new FutureCombiner<VirtualMachine>();
		for (VirtualMachine vm : linuxVms) {
			CompletableFuture<VirtualMachine> cf = serviceProvider.getTestUpDown().startFutures(vm, false);
			// cf = serviceProvider.getAwsUpdateStatus().chainFutures(cf, VmState.STOPPING);
			cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			logger.error("NEED TO FIGURE OUT STOP SERVICES");
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.STOPPED);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			fc.addFuture(cf);
		}
		fc.combineFutures(linuxFuture);
	}

	public void deleteGuests(Collection<VirtualMachine> linuxVms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		Collection<String> hostnames = new ArrayList<String>();
		try {
			for (VirtualMachine vm : linuxVms) {
				if (JavaUtil.isNotEmpty(vm.getInternalHostname())) {
					hostnames.add(vm.getInternalHostname());
				}
			}
			if (!hostnames.isEmpty()) {
				route53.deleteARecords(hostnames);
			}
		} catch (Exception e) {
			logger.error("Failed to delete hostnames from DNS.  Hostnames=" + hostnames, e);
		} finally {
			linuxFuture.complete(linuxVms);
		}
	}
}
