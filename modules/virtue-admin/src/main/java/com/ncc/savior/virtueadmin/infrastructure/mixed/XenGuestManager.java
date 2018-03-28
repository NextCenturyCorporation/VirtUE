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
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class XenGuestManager {
	private static final Logger logger = LoggerFactory.getLogger(XenGuestManager.class);
	private static final String VM_PREFIX = "VRTU-";
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
		JSch ssh = new JSch();
		ChannelExec channel = null;
		Session session = null;

		try {
			ssh.addIdentity(keyFile.getAbsolutePath());
			session = ssh.getSession(xenVm.getUserName(), xenVm.getHostname(), xenVm.getSshPort());
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

			Collection<VirtualMachine> vms = virtue.getVms();
			Iterator<VirtualMachine> vmsItr = vms.iterator();
			int externalPort = 8001;
			for (VirtualMachineTemplate vmt : linuxVmts) {
				VirtualMachine vm = vmsItr.next();
				String ipAddress = "0.0.0.0";
				String clientUser = virtue.getUsername();
				String domainUUID = UUID.randomUUID().toString();
				String name = VM_PREFIX + clientUser + "-" + virtue.getUsername() + "-" + domainUUID;
				// String loginUsername = vmt.getLoginUser();
				String loginUsername = "user";

				ps.println("sudo xl list");
				ps.println("cd ./app-domains");
				ps.println("sudo ./create.sh " + name);
				ps.println("sudo xl console " + name);
				JavaUtil.sleepAndLogInterruption(200);
				ipAddress = getIpAddress(br);
				String finishString = "finished with " + domainUUID;
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						String line;
						try {
							while ((line = br.readLine()) != null) {
								System.out.println(line);
								if (line.contains(finishString) && !line.contains("echo")) {
									break;
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				t.start();
				ps.println("\035");
				JavaUtil.sleepAndLogInterruption(1000);
				ps.println("sudo xl list");
				String dnsAddress = ""; // we don't have dns name yet.
				vm.setName(name);
				vm.setInfrastructureId(name);
				vm.setUserName(loginUsername);
				vm.setHostname(dnsAddress);
				vm.setIpAddress(ipAddress);
				int internalPort = 22;
				String cmd = String.format(
						"sudo iptables -t nat -A PREROUTING -p tcp -i eth0  --dport %d -j DNAT --to-destination %s:%d ;",
						externalPort, ipAddress, internalPort);
				cmd += String.format(
						"sudo iptables -A FORWARD -p tcp -d %s --dport %d  -m state --state NEW,ESTABLISHED,RELATED -j ACCEPT",
						ipAddress, internalPort);
				cmd += ";sudo ip route";
				JavaUtil.sleepAndLogInterruption(10000);
				ps.println(cmd);
				ps.println("echo " + finishString);
				JavaUtil.sleepAndLogInterruption(1000);
				t.join(20000);
				// TODO we assume they are running, but this is somewhat foolish.
				vm.setState(VmState.LAUNCHING);
				vm.setHostname(xenVm.getHostname());
				vm.setIpAddress(xenVm.getIpAddress());
				vm.setPrivateKeyName(xenVm.getPrivateKeyName());
				vm.setSshPort(externalPort);
				vm.setApplications(new ArrayList<ApplicationDefinition>(vmt.getApplications()));
				externalPort++;
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

	/**
	 * @param input
	 * @param channel
	 */
	public static String getIpAddress(BufferedReader reader) throws Exception {
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

}
