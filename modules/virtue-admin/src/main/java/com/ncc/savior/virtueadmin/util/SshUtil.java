package com.ncc.savior.virtueadmin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Utility functions related to SSH.
 */
public class SshUtil {

	private static final Logger logger = LoggerFactory.getLogger(SshUtil.class);

	public static void waitUtilVmReachable(VirtualMachine vm, String privateKeyFile, long periodMillis) {
		while (!isVmReachable(vm, privateKeyFile)) {
			JavaUtil.sleepAndLogInterruption(periodMillis);
		}
	}

	// untested
	// public static void waitForAllVmsReachableSerially(ArrayList<VirtualMachine>
	// vms, int periodMillis) {
	// for (VirtualMachine vm : vms) {
	// waitUtilVmReachable(vm, periodMillis);
	// }
	// }

	public static List<String> sendCommandFromSession(Session session, String command)
			throws JSchException, IOException {
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
				logger.debug("  : " + line);
				lines.add(line);
			}
			logger.debug("finished command successfully");
			return lines;
		} catch (Exception e) {
			logger.debug("finished command exceptionally", e);
			throw e;
		} finally {
			JavaUtil.closeIgnoreErrors(br, er);
			myChannel.disconnect();
		}
	}

	public static void waitForAllVmsReachableParallel(Collection<VirtualMachine> vms, int periodMillis) {
		// create copy so we can modify the list
		vms = new ArrayList<VirtualMachine>(vms);
		boolean allReachable = false;
		do {
			allReachable = true;
			// use iterator so we can remove without modification exceptions
			Iterator<VirtualMachine> itr = vms.iterator();
			while (itr.hasNext()) {
				VirtualMachine vm = itr.next();
				boolean thisVmReachable = isVmReachable(vm, vm.getPrivateKey());
				allReachable &= thisVmReachable;
				if (thisVmReachable) {
					itr.remove();
				} else {
					break;
				}
				// if we never break, all vms must be reachable
				allReachable = true;
			}
			JavaUtil.sleepAndLogInterruption(periodMillis);
		} while (!allReachable);
	}

	public static boolean isVmReachable(VirtualMachine vm, String privateKey) {
		File key = null;
		try {
			key = File.createTempFile("test", "");
			FileWriter writer = new FileWriter(key);
			writer.write(vm.getPrivateKey());
			writer.close();
			return isVmReachable(vm, key);
		} catch (IOException e) {
			logger.trace("Error writing key to test VM reachability: " + e.getMessage());
			return false;
		} finally {
			if (key != null) {
				key.delete();
			}
		}
	}

	public static boolean isVmReachable(VirtualMachine vm, File privateKeyFile) {
		// Jsch is not thread safe
		JSch ssh = new JSch();
		ChannelExec channel = null;
		Session session = null;
		// BufferedReader reader = null;
		// BufferedReader ereader = null;
		try {
			session = SshUtil.getConnectedSession(vm, privateKeyFile, ssh);
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("echo 'Testing reachability of VM'");
			channel.connect(0);

			// InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			// reader = new BufferedReader(stream);
			// InputStreamReader estream = new InputStreamReader(channel.getErrStream());
			// ereader = new BufferedReader(estream);
			// String line;
			// logger.debug("should read line soon");
			// while ((line = reader.readLine()) != null || (line = ereader.readLine()) !=
			// null) {
			// logger.trace(line);
			// }
			return true;
		} catch (JSchException e) {
			if (e.getMessage().contains("Auth fail")) {
				throw new SaviorException(SaviorException.CONFIGURATION_ERROR, "Auth fail trying to login to vm=" + vm);
			}
			logger.trace("Vm is not reachable yet: " + e.getMessage());
			return false;
		} catch (Throwable t) {
			logger.error("Found unexpected error.  Handle me better", t);
			return false;
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
			// JavaUtil.closeIgnoreErrors(reader, ereader);
		}
	}

	public static Session getConnectedSession(VirtualMachine vm, File privateKeyFile) throws JSchException {
		JSch ssh = new JSch();
		return getConnectedSession(vm, privateKeyFile, ssh);
	}

	public static Session getConnectedSession(VirtualMachine vm, File privateKeyFile, JSch ssh) throws JSchException {
		Session session;
		ssh.addIdentity(privateKeyFile.getAbsolutePath());
		session = ssh.getSession(vm.getUserName(), vm.getHostname(), vm.getSshPort());
		session.setConfig("PreferredAuthentications", "publickey");
		session.setConfig("StrictHostKeyChecking", "no");
		session.setTimeout(1000);
		session.connect();
		return session;
	}

	public static String getKeyFromFile(File privateKey) {
		FileReader reader = null;
		if (privateKey == null || !privateKey.isFile()) {
			return "";
		}
		try {
			reader = new FileReader(privateKey);
			char[] cbuf = new char[4096];
			int n = reader.read(cbuf);
			String s = new String(cbuf, 0, n);
			return s;
		} catch (IOException e) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR,
					"Error attempting to read file=" + privateKey.getAbsolutePath(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("Error attempting to close file=" + privateKey.getAbsolutePath());
				}
			}
		}
	}

	public static void writeKeyToFile(File certificate, String privateKey) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(certificate);
			writer.write(privateKey);
			writer.flush();
		} catch (IOException e) {
			logger.warn("Failed to write key to file", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.warn("Failed to close file writer", e);
				}
			}
		}

	}

}
