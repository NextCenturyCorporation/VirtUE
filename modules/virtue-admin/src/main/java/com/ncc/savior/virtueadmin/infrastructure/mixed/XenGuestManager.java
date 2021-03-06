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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.FutureCombiner;
import com.ncc.savior.virtueadmin.infrastructure.aws.Route53Manager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;

/**
 * Handles creation, starting, stopping, deleting and some other management of
 * XenGuest (DomU) Virtual Machines. One instance of this class maps directly to
 * only one instance of Xen.
 * 
 *
 */
public class XenGuestManager {
	private static final Logger logger = LoggerFactory.getLogger(XenGuestManager.class);
	private static final String PORTS_FILE = "ports.properties";
	private File keyFile;
	private VirtualMachine xenVm;
	private Route53Manager route53;
	private CompletableFutureServiceProvider serviceProvider;
	private int numSensingPorts = 10;

	private ITemplateService templateService;

	public XenGuestManager(VirtualMachine xenVm, File keyFile, CompletableFutureServiceProvider serviceProvider,
			Route53Manager route53, ITemplateService templateService) {
		this.keyFile = keyFile;
		this.xenVm = xenVm;
		this.serviceProvider = serviceProvider;
		this.route53 = route53;
		this.templateService = templateService;
	}

	public void provisionGuests(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts,
			CompletableFuture<Collection<VirtualMachine>> xenGuestFuture, String serverUser) {

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
				vm.setUserName(vmt.getLoginUser());
				vm.setState(VmState.LAUNCHING);
				serviceProvider.getVmNotifierService().startFutures(vm, null);
				createStartGuestVm(session, externalSshPort, externalSensingPort, startingInternalPort, numSensingPorts,
						vmt.getTemplatePath(), vmt.getSecurityTag(), vm, true);
				externalSensingPort += numSensingPorts;
				vm.setApplications(new ArrayList<ApplicationDefinition>(vmt.getApplications()));
				vm.setPrivateKeyName(xenVm.getPrivateKeyName());
				JavaUtil.sleepAndLogInterruption(100);
				externalSshPort++;
				logger.debug("finished provisioning linux guest Vm=" + vm.getName());
			}
			logger.debug("finished provisioning of linux guest VMs=" + linuxVmts);
			addVmToProvisionPipeline(linuxVms, xenGuestFuture);
		} catch (JSchException e) {
			logger.error("Vm is not reachable yet: ", e);
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

	private void createStartGuestVm(Session session, int externalSshPort, int externalSensingPort,
			int startingInternalPort, int numSensingPorts, String templatePath, String role, VirtualMachine vm,
			boolean create) throws JSchException, IOException, SftpException, TemplateException {

		String ipAddress = "0.0.0.0";
		String name = vm.getName();
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("vm", vm);
		model.put("templatePath", templatePath);
		model.put("securityRole", (role == null ? "" : role));
		if (create) {
			List<String> lines = SshUtil.runCommandsFromFile(templateService, session, "xen-guest-create.tpl", model);
			logger.debug("provision guest output: " + lines);
		} else {
			// error:
			// xencall: error: Could not obtain handle on privileged command interface: No
			// such file or directory
			// libxl: error: libxl.c:102:libxl_ctx_alloc: cannot open libxc handle: No such
			// file or directory
			// cannot init xl context
			boolean success = false;
			while (true) {
				success = true;
				// List<String> response = SshUtil.sendCommandFromSession(session,
				// "sudo xl create app-domains/" + name + "/" + name + ".cfg");
				List<String> response = SshUtil.runCommandsFromFile(templateService, session, "xen-guest-start.tpl",
						model);
				for (String line : response) {
					if (line.contains("xencall: error:") || line.contains("invalid domain identifier")) {
						success = false;
						break;
					}
				}
				if (success) {
					break;
				} else {
					JavaUtil.sleepAndLogInterruption(200);
				}
			}
		}
		ipAddress = getGuestVmIpAddress(session, name);
		updateSshKnownHosts(session, ipAddress);
		logger.debug("Attempting to setup port forwarding. ");
		String hostname = SshUtil.runCommand(session, "hostname", 1000).getOutput().trim();
		if (hostname.isEmpty()) {
			hostname = "custom-" + ipAddress.replaceAll("\\.", "-");
			logger.debug("hostname was not found!  Using " + hostname);
		}
		String dns = route53.AddARecord(hostname, xenVm.getInternalIpAddress());
		// setupHostname(session, loginUsername, hostname, dns, ipAddress);
		setupPortForwarding(session, externalSensingPort, startingInternalPort, numSensingPorts, ipAddress);

		externalSensingPort += numSensingPorts;
		// catFile(session, ipAddress, loginUsername, PORTS_FILE);
		// startSensors(session, SENSOR_SCRIPT, ipAddress, loginUsername);

		vm.setInfrastructureId(name);
		vm.setHostname(xenVm.getHostname());
		vm.setIpAddress(xenVm.getIpAddress());
		vm.setInternalHostname(dns);
		vm.setInternalIpAddress(ipAddress);
		vm.setSshPort(externalSshPort);

		int internalPort = 22;
		model.put("internalSshPort", internalPort);
		model.put("externalDns", dns);
		model.put("hostname", hostname);
		model.put("xenInternalIpAddress", ipAddress);
		model.put("vm", vm);
		model.put("portsFile", PORTS_FILE);
		List<String> lines = SshUtil.runCommandsFromFileWithTimeout(templateService, session,
				"xen-guest-start-sensors.tpl", model, 200);
		logger.debug("output: " + lines);

		// setupPortForward(session, externalSshPort, ipAddress, internalPort);
		// printIpTables(session);
	}

	private void updateSshKnownHosts(Session session, String ipAddress) throws JSchException, IOException {
		String sshConfig = SshUtil.runCommand(session, "cat ~/.ssh/known_hosts", 5000).getOutput();
		boolean hostIsKnown = sshConfig.contains(ipAddress);
		if (!hostIsKnown) {
			String hostCmd = "ssh-keyscan " + ipAddress + " >> ~/.ssh/known_hosts";
			List<String> logOutput = SshUtil.sendCommandFromSession(session, hostCmd);
			logger.debug(logOutput.toString());
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

	private String getGuestVmIpAddress(Session session, String name) throws JSchException, IOException {
		String ipAddress;
		logger.debug("Attempting to get IP address of guest.");
		CommandHandler ch = getCommandHandlerFromSession(session);
		ipAddress = getIpFromConsole(ch, name);
		return ipAddress;
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

	private void setupPortForwarding(Session session, int externalSensingPort, int startingInternalPort,
			int numSensingPorts, String ipAddress) throws JSchException, IOException, SftpException, TemplateException {
		ArrayList<String> lines = new ArrayList<String>();
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put("guestIp", ipAddress);
		dataModel.put("externalPort", externalSensingPort );
		dataModel.put("internalPort", startingInternalPort );
		dataModel.put("numSensingPorts", numSensingPorts);
		List<String> o = SshUtil.runScriptFromFile(templateService, session, "port-forwarding.tpl", dataModel);
		lines.addAll(o);
//		for (int i = 0; i < numSensingPorts; i++) {
//			dataModel.put("externalPort", externalSensingPort + i);
//			dataModel.put("internalPort", startingInternalPort + i);
//			List<String> o = SshUtil.runScriptFromFile(templateService, session, "port-forwarding.tpl", dataModel);
//			lines.addAll(o);
//			JavaUtil.sleepAndLogInterruption(100);
//		}
		JavaUtil.sleepAndLogInterruption(100);
		logger.debug(lines.toString());
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
			if (line.contains("virtue-ip") || line.contains("My IP address")) {
				virtue_ip = findIP(line);
				return virtue_ip;
			} else if (line.contains("invalid domain identifier")) {
				throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, "Unable to get ip address for domU");
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
				int externalSshPort = 8001;
				int externalSensingPort = 12001;
				int startingInternalPort = 11001;
				vm.setState(VmState.LAUNCHING);
				serviceProvider.getVmNotifierService().startFutures(vm, null);
				// throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "Restarting is not
				// properly implemented!");
				createStartGuestVm(session, externalSshPort, externalSensingPort, startingInternalPort, numSensingPorts,
						null, null, vm, false);
			}
			addToStartPipeline(linuxVms, linuxFuture);
		} catch (Exception e) {
			logger.error("Error attempting to start guests " + linuxVms, e);
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
			// cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
			cf = serviceProvider.getUpdateStatus().startFutures(vm, VmState.LAUNCHING);
			cf = serviceProvider.getNetworkSettingService().chainFutures(cf, xenVm);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			// cf.exceptionally((ex) -> {
			// logger.error("EXCEPTION", ex);
			// linuxFuture.completeExceptionally(ex);
			// vm.setState(VmState.ERROR);
			// return xenVm;
			// });
			fc.addFuture(cf);
		}
		fc.combineFutures(linuxFuture);
		linuxFuture.exceptionally((ex) -> {
			logger.error("lf", ex);
			// vm.setState(VmState.ERROR);
			return null;
		});
	}

	public void stopGuests(Collection<VirtualMachine> linuxVms,
			CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Session session = null;
		try {
			session = SshUtil.getConnectedSession(xenVm, keyFile);
			for (VirtualMachine vm : linuxVms) {
				// SshUtil.sendCommandFromSession(session, "sudo xl shutdown " + vm.getName());
				Map<String, Object> dataModel = new HashMap<String, Object>();
				dataModel.put("vm", vm);
				SshUtil.runCommandsFromFile(templateService, session, "xen-guest-stop.tpl", dataModel);
			}
			addToStopPipeline(linuxVms, linuxFuture);
		} catch (JSchException | IOException | TemplateException e) {
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
			CompletableFuture<VirtualMachine> cf = serviceProvider.getUpdateStatus().startFutures(vm, VmState.STOPPING);
			cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
			cf = serviceProvider.getTestUpDown().startFutures(vm, false);
			cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.STOPPED);
			cf = serviceProvider.getNetworkClearingService().chainFutures(cf, v);
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
		Void v = null;
		Collection<String> hostnames = new ArrayList<String>();
		try {
			for (VirtualMachine vm : linuxVms) {
				if (JavaUtil.isNotEmpty(vm.getInternalHostname())) {
					hostnames.add(vm.getInternalHostname());
				}
				CompletableFuture<VirtualMachine> cf = serviceProvider.getUpdateStatus().startFutures(vm,
						VmState.DELETING);
				serviceProvider.getVmNotifierService().chainFutures(cf, v);
			}
			// TODO roll route53 into a microservice
			if (!hostnames.isEmpty()) {
				try {
					route53.deleteARecords(hostnames);
				} catch (AmazonClientException e) {
					logger.warn("Failed to delete DNS record for one of " + hostnames);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to delete hostnames from DNS.  Hostnames=" + hostnames, e);
		} finally {
			linuxFuture.complete(linuxVms);
		}
	}

	public VirtualMachine startVirtualMachine(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> vmFuture,
			String virtue) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms.add(vm);
		vms = startVirtualMachines(vms, vmFuture, virtue);
		return vms.iterator().next();
	}

	public VirtualMachine stopVirtualMachine(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> vmFuture,
			String virtue) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		vms.add(vm);
		vms = stopVirtualMachines(vms, vmFuture, virtue);
		return vms.iterator().next();
	}

	public Collection<VirtualMachine> startVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> vmFuture, String virtue) {
		if (vmFuture == null) {
			vmFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (vms.isEmpty()) {
			vmFuture.complete(vms);
		} else {
			startGuests(vms, vmFuture);
		}
		return vms;
	}

	public Collection<VirtualMachine> stopVirtualMachines(Collection<VirtualMachine> vms,
			CompletableFuture<Collection<VirtualMachine>> vmFuture, String virtue) {
		if (vmFuture == null) {
			vmFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		if (vms.isEmpty()) {
			vmFuture.complete(vms);
		} else {
			stopGuests(vms, vmFuture);
		}
		return vms;
	}

	public void rebootVm(VirtualMachine vm, CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		rebootVms(vms, linuxFuture);
	}

	public void rebootVms(Collection<VirtualMachine> vms, CompletableFuture<Collection<VirtualMachine>> linuxFuture) {
		Session session = null;
		if (linuxFuture == null) {
			linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		}
		try {
			CompletableFuture<Collection<VirtualMachine>> stopFuture = new CompletableFuture<Collection<VirtualMachine>>();
			session = SshUtil.getConnectedSession(xenVm, keyFile);
			for (VirtualMachine vm : vms) {
				SshUtil.sendCommandFromSession(session, "sudo xl reboot " + vm.getName());
			}
			CompletableFuture<Collection<VirtualMachine>> vmFutureFinal = linuxFuture;
			addToStopPipeline(vms, stopFuture);
			stopFuture.thenAccept((Collection<VirtualMachine> stoppedVm) -> {
				addToStartPipeline(stoppedVm, vmFutureFinal);
			});
		} catch (JSchException | IOException e) {
			linuxFuture.completeExceptionally(e);
		} finally {
			SshUtil.disconnectLogErrors(session);
		}
	}

	public void setNumSensingPorts(int numSensingPorts) {
		this.numSensingPorts = numSensingPorts;
	}
}
