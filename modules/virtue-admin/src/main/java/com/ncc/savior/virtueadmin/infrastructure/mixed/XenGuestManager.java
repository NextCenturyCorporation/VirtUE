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
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.virtueadmin.infrastructure.DirectoryKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class XenGuestManager {
	private static final Logger logger = LoggerFactory.getLogger(XenGuestManager.class);
	private static final String VM_PREFIX = "VRTU-";
	private static final String PORTS_FILE = "ports.properties";
	private File keyFile;
	private VirtualMachine xenVm;
	private IUpdateListener<VirtualMachine> notifier;
	private XenGuestVmUpdater guestUpdater;

	public XenGuestManager(VirtualMachine xenVm, File keyFile, IUpdateListener<VirtualMachine> notifier,
			XenGuestVmUpdater guestUpdater) {
		this.keyFile = keyFile;
		this.xenVm = xenVm;
		this.notifier = notifier;
		this.guestUpdater = guestUpdater;
	}

	public void provisionGuests(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts) {

		ChannelExec channel = null;
		Session session = null;

		try {
			session = getConnectedSession();

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

				VirtualMachine vm = vmsItr.next();
				String ipAddress = "0.0.0.0";
				String clientUser = virtue.getUsername();
				String domainUUID = UUID.randomUUID().toString();
				String name = VM_PREFIX + clientUser + "-" + virtue.getUsername() + "-" + domainUUID;

				name = "VRTU-test";
				// ipAddress = "192.168.0.54";
				String loginUsername = "user";
				// String loginUsername = vmt.getLoginUser();
				createGuestVm(session, name);
				ipAddress = getGuestVmIpAddress(session, name);
				List<String> sshConfig = sendCommandFromSession(session, "cat ~/.ssh/known_hosts");
				boolean hostIsKnown = false;
				for (String line : sshConfig) {
					if (line.contains(ipAddress)) {
						hostIsKnown = true;
						break;
					}
				}
				if (!hostIsKnown) {
					String hostCmd = "ssh-keyscan " + ipAddress + " >> ~/.ssh/known_hosts";
					sendCommandFromSession(session, hostCmd);
				}

				logger.debug("Attempting to setup port forwarding.");
				String hostname = sendCommandFromSession(session, "hostname").get(0);
				hostname += ".ec2.internal";
				setupHostname(session, loginUsername, hostname, ipAddress);
				setupPortForwarding(session, externalSensingPort, startingInternalPort, numSensingPorts, ipAddress);
				externalSensingPort += numSensingPorts;
				catFile(session, ipAddress, loginUsername, PORTS_FILE);

				String dnsAddress = ""; // we don't have dns name yet.
				vm.setName(name);
				vm.setInfrastructureId(name);
				vm.setUserName(loginUsername);
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
				sendCommandFromSession(session, cmd);
				sendCommandFromSession(session, "sudo iptables -vnL -t nat\n");
				JavaUtil.sleepAndLogInterruption(1000);
				vm.setState(VmState.LAUNCHING);
				vm.setHostname(xenVm.getHostname());
				vm.setIpAddress(xenVm.getIpAddress());
				vm.setPrivateKeyName(xenVm.getPrivateKeyName());
				vm.setSshPort(externalSshPort);
				vm.setApplications(new ArrayList<ApplicationDefinition>(vmt.getApplications()));
				externalSshPort++;
			}
			notifier.updateElements(vms);
			guestUpdater.addVmToProvisionPipeline(vms);
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
	}

	private String getGuestVmIpAddress(Session session, String name) throws JSchException, IOException {
		String ipAddress;
		logger.debug("Attempting to get IP address of guest.");
		CommandHandler ch = getCommandHandlerFromSession(session);
		ipAddress = getIpFromConsole(ch, name);
		return ipAddress;
	}

	private String createGuestVm(Session session, String name) throws JSchException, IOException {
		CommandHandler ch = getCommandHandlerFromSession(session);
		String finishString = "finished with " + name;
		logger.debug("Sending commands to create VM.");
		ch.sendln("sudo xl list");
		ch.sendln("cd ./app-domains");
		ch.sendln("sudo ./create.sh " + name);
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

	private List<String> sendCommandFromSession(Session session, String command) throws JSchException, IOException {
		logger.debug("sending command: " + command);
		ChannelExec myChannel = (ChannelExec) session.openChannel("exec");
		BufferedReader br = null;
		BufferedReader er = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			myChannel.setCommand(command);
			// OutputStream ops = myChannel.getOutputStream();
			// PrintStream ps = new PrintStream(ops, true);
			myChannel.connect();
			InputStream input = myChannel.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			br = new BufferedReader(reader);
			er = new BufferedReader(new InputStreamReader(myChannel.getErrStream()));
			String line;
			JavaUtil.sleepAndLogInterruption(500);
			while ((line = br.readLine()) != null || (line = er.readLine()) != null) {
				logger.debug(line);
				lines.add(line);
			}
			return lines;
		} finally {
			JavaUtil.closeIgnoreErrors(br, er);
			myChannel.disconnect();
		}
	}

	private Session getConnectedSession() throws JSchException {
		JSch ssh = new JSch();
		Session session;
		ssh.addIdentity(keyFile.getAbsolutePath());
		session = ssh.getSession(xenVm.getUserName(), xenVm.getHostname(), xenVm.getSshPort());
		session.setConfig("PreferredAuthentications", "publickey");
		session.setConfig("StrictHostKeyChecking", "no");
		session.setTimeout(500);
		session.connect();
		return session;
	}

	private void setupHostname(Session session, String username, String hostname, String ipAddress)
			throws JSchException, IOException {
		// hostname = hostname`";
		String cmd = "ssh -i virginiatech_ec2.pem " + username + "@" + ipAddress + " \"echo hostname=" + hostname
				+ " > " + PORTS_FILE + "\"";
		sendCommandFromSession(session, cmd);
	}

	private void setupPortForwarding(Session session, int externalSensingPort, int startingInternalPort,
			int numSensingPorts, String ipAddress) throws JSchException, IOException {
		for (int i = 0; i < numSensingPorts; i++) {

			String cmd = "./portForwarding2.sh " + (i + externalSensingPort) + " " + ipAddress + " "
					+ (startingInternalPort + i);
			sendCommandFromSession(session, cmd);
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
		sendCommandFromSession(session, cmd);
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

	public void stopGuests(Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	public void deleteGuests(Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		IUpdateListener<VirtualMachine> updateListener = new IUpdateListener<VirtualMachine>() {
			@Override
			public void updateElements(Collection<VirtualMachine> elements) {
				logger.debug("Updated " + elements);
			}
		};
		String hostname = "ec2-34-229-246-30.compute-1.amazonaws.com";
		String ipAddress = "34.229.246.30";
		VirtualMachine xenVm = new VirtualMachine(UUID.randomUUID().toString(), "Test VM",
				new ArrayList<ApplicationDefinition>(), VmState.RUNNING, OS.LINUX, "", hostname, 22, "ec2-user", null,
				"virginiatech_ec2", ipAddress);
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms.add(new VirtualMachine(UUID.randomUUID().toString(), "Test VM", new ArrayList<ApplicationDefinition>(),
				VmState.CREATING, OS.LINUX, "test id", "", 22, "user", "", "", ""));
		VirtueInstance virtue = new VirtueInstance(UUID.randomUUID().toString(), "test-virtue", "test-user",
				"test_template id", new ArrayList<ApplicationDefinition>(), vms);
		Collection<VirtualMachineTemplate> linuxVmts = new ArrayList<VirtualMachineTemplate>();
		linuxVmts.add(new VirtualMachineTemplate(UUID.randomUUID().toString(), "test template", OS.LINUX, "test",
				new ArrayList<ApplicationDefinition>(), "user", true, new Date(), "System"));

		XenGuestManager mgr = new XenGuestManager(xenVm, new File("certs/virginiatech_ec2.pem"), updateListener,
				new XenGuestVmUpdater(updateListener, new DirectoryKeyManager(new File("./certs/"))));

		mgr.provisionGuests(virtue, linuxVmts);
	}

}
