package com.ncc.savior.virtueadmin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

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

	public static void waitUtilVmReachable(VirtualMachine vm, long periodMillis) {
		while (!isVmReachable(vm)) {
			sleepAndLogInterruption(periodMillis);
		}
	}

	private static void sleepAndLogInterruption(long periodMillis) {
		try {
			Thread.sleep(periodMillis);
		} catch (InterruptedException e) {
			logger.error("Unexpected sleep interruption!", e);
		}
	}

	// untested
	// public static void waitForAllVmsReachableSerially(ArrayList<VirtualMachine>
	// vms, int periodMillis) {
	// for (VirtualMachine vm : vms) {
	// waitUtilVmReachable(vm, periodMillis);
	// }
	// }

	public static void waitForAllVmsReachableParallel(ArrayList<VirtualMachine> vms, int periodMillis) {
		// create copy so we can modify the list
		vms = new ArrayList<VirtualMachine>(vms);
		boolean allReachable = false;
		do {
			allReachable = true;
			// use iterator so we can remove without modification exceptions
			Iterator<VirtualMachine> itr = vms.iterator();
			while (itr.hasNext()) {
				VirtualMachine vm = itr.next();
				boolean thisVmReachable = isVmReachable(vm);
				allReachable &= thisVmReachable;
				if (thisVmReachable) {
					itr.remove();
				} else {
					break;
				}
				// if we never break, all vms must be reachable
				allReachable = true;
			}
			sleepAndLogInterruption(periodMillis);
		} while (!allReachable);
	}

	public static boolean isVmReachable(VirtualMachine vm) {
		// Jsch is not thread safe
		JSch ssh = new JSch();
		File key = null;
		ChannelExec channel = null;
		Session session = null;
		try {
			key = File.createTempFile("test", "");
			FileWriter writer = new FileWriter(key);
			writer.write(vm.getPrivateKey());
			writer.close();
			ssh.addIdentity(key.getAbsolutePath());
			session = ssh.getSession(vm.getUserName(), vm.getHostname(), vm.getSshPort());
			session.setConfig("PreferredAuthentications", "publickey");
			session.setConfig("StrictHostKeyChecking", "no");
			session.setTimeout(500);
			session.connect();
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("echo 'test'");
			channel.connect();

			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			InputStreamReader estream = new InputStreamReader(channel.getErrStream());
			BufferedReader ereader = new BufferedReader(estream);
			String line;
			// logger.debug("should read line soon");
			while ((line = reader.readLine()) != null || (line = ereader.readLine()) != null) {
				logger.trace(line);
			}
		} catch (JSchException | IOException e) {
			logger.trace("Vm is not reachable yet: " + e.getMessage());
			return false;
		} finally {
			if (key != null) {
				key.delete();
			}
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
		return true;
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

}
