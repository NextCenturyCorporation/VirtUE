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
package com.ncc.savior.util;

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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;

/**
 * Utility functions related to SSH.
 */
public class SshUtil {

	public static class SshResult {

		private String output;
		private String error;
		private int exitStatus;

		public SshResult(String output, String error, int exitStatus) {
			this.output = output;
			this.error = error;
			this.exitStatus = exitStatus;
		}

		public String getOutput() {
			return output;
		}

		public String getError() {
			return error;
		}

		public int getExitStatus() {
			return exitStatus;
		}

	}

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

	public static ChannelExec getChannelExecFromCommand(Session session, String command) throws JSchException {
		logger.debug("sending command: " + command);
		ChannelExec myChannel = (ChannelExec) session.openChannel("exec");
		myChannel.setCommand(command);
		return myChannel;
	}

	public static List<String> sendCommandFromSession(Session session, String command)
			throws JSchException, IOException {
		ChannelExec myChannel = null;
		BufferedReader br = null;
		BufferedReader er = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			myChannel = getChannelExecFromCommand(session, command);
			myChannel.setPty(true);
			InputStream input = myChannel.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			br = new BufferedReader(reader);
			er = new BufferedReader(new InputStreamReader(myChannel.getErrStream()));
			myChannel.connect();
			String line;
			JavaUtil.sleepAndLogInterruption(500);
			while ((line = br.readLine()) != null || (line = er.readLine()) != null) {
				lines.add(line);
			}
			// logger.debug("finished command successfully");
			return lines;
		} catch (Exception e) {
			logger.debug("finished command exceptionally", e);
			throw e;
		} finally {
			JavaUtil.closeIgnoreErrors(br, er);
			myChannel.disconnect();
		}
	}

	public static List<String> sendCommandFromSessionWithTimeout(Session session, String command, long waitTimeMillis)
			throws JSchException, IOException {
		ChannelExec myChannel = null;
		InputStreamReader er = null;
		InputStreamReader reader = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			myChannel = getChannelExecFromCommand(session, command);
			InputStream input = myChannel.getInputStream();
			reader = new InputStreamReader(input);

			er = new InputStreamReader(myChannel.getErrStream());
			myChannel.connect();
			JavaUtil.sleepAndLogInterruption(waitTimeMillis);
			String error = readAvailable(er);
			String output = readAvailable(reader);
			lines.add(output);
			lines.add(error);
			logger.debug("finished command successfully");
			return lines;
		} catch (Exception e) {
			logger.debug("finished command exceptionally", e);
			throw e;
		} finally {
			JavaUtil.closeIgnoreErrors(reader, er);
			myChannel.disconnect();
		}
	}

	public static void sftpFile(Session session, InputStream source, String destinationFilePath)
			throws SftpException, JSchException {
		ChannelSftp ch = null;
		try {
			ch = (ChannelSftp) session.openChannel("sftp");
			ch.connect();
			ch.put(source, destinationFilePath);
		} finally {
			if (ch != null) {
				ch.disconnect();
			}
		}
	}

	/**
	 * This function explicitly reads only what is available and does not wait until
	 * the stream is finished. Therefore, some of the stream may be lost.
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private static String readAvailable(InputStreamReader reader) throws IOException {
		if (reader.ready()) {
			int size = 4096;
			char[] cbuf = new char[size];
			int offset = 0;
			StringBuilder sb = new StringBuilder();
			while (reader.ready()) {
				int numRead = reader.read(cbuf, offset, size);
				sb.append(cbuf, offset, numRead);
			}
			return sb.toString();
		}
		return "";
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
			// if (e.getMessage().contains("Auth fail")) {
			// throw new SaviorException(SaviorException.CONFIGURATION_ERROR, "Auth fail
			// trying to login to vm=" + vm);
			// }
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

	/**
	 * Utilizes an {@link ITemplateService} to send commands from a template file on
	 * a session. Each line of the templated script will be run separately.
	 * 
	 * @param templateService
	 * @param session
	 * @param templateName
	 * @param dataModel
	 * @return
	 * @throws TemplateException
	 * @throws IOException
	 * @throws JSchException
	 */
	public static List<String> runCommandsFromFile(ITemplateService templateService, Session session,
			String templateName, Map<String, Object> dataModel) throws TemplateException, JSchException, IOException {
		String[] lines = templateService.processTemplateToLines(templateName, dataModel);
		List<String> output = new ArrayList<String>();
		for (String line : lines) {
			List<String> o = SshUtil.sendCommandFromSession(session, line);
			output.addAll(o);
		}
		return output;
	}

	public static List<String> runScriptFromFile(ITemplateService templateService, Session session, String templateName,
			Map<String, Object> dataModel) throws JSchException, IOException, TemplateException {
		String[] lines = templateService.processTemplateToLines(templateName, dataModel);
		String line = String.join("\n", lines);
		List<String> o = SshUtil.sendCommandFromSession(session, line);
		return o;
	}

	public static List<String> runCommandsFromFileWithTimeout(ITemplateService templateService, Session session,
			String templateName, Map<String, Object> dataModel, long timeoutMillis)
			throws TemplateException, JSchException, IOException {
		String[] lines = templateService.processTemplateToLines(templateName, dataModel);
		List<String> output = new ArrayList<String>();
		for (String line : lines) {
			List<String> o = SshUtil.sendCommandFromSessionWithTimeout(session, line, timeoutMillis);
			output.addAll(o);
		}
		return output;
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
			throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR,
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

	public static void disconnectLogErrors(Session session, Channel channel) {
		try {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
		} catch (Throwable t) {
			logger.warn("Error attempting to disconnect SSH channel.", t);
		}
		disconnectLogErrors(session);
	}

	public static void disconnectLogErrors(Session session) {
		try {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		} catch (Throwable t) {
			logger.warn("Error attempting to disconnect SSH session.", t);
		}
	}

	public static Session getConnectedSessionWithRetries(VirtualMachine vm, File privateKeyFile, int numTries,
			int timeBetweenTriesMillis) throws JSchException {
		JSchException lastException = null;
		do {
			try {
				return getConnectedSession(vm, privateKeyFile);
			} catch (JSchException e) {
				numTries--;
				lastException = e;
			}
		} while (numTries > 0);
		throw lastException;
	}

	public static SshResult runTemplateFile(ITemplateService templateService, Session session, String templateName,
			Map<String, ?> dataModel, int timeoutMillis) throws TemplateException, JSchException, IOException {
		String command = String.join("\n", templateService.processTemplateToLines(templateName, dataModel));
		return runCommand(session, command, timeoutMillis);
	}

	public static SshResult runCommand(Session session, String command, int timeoutMillis)
			throws JSchException, IOException {
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		logger.debug("running command: " + command);
		channel.setCommand(command);
		InputStream inputStream = channel.getInputStream();
		InputStream extInputStream = channel.getExtInputStream();
		StringBuilder inputBuilder = new StringBuilder();
		InputStreamConsumer inputConsumer = new InputStreamConsumer(inputStream, inputBuilder);
		StringBuilder extInputBuilder = new StringBuilder();
		InputStreamConsumer extInputConsumer = new InputStreamConsumer(extInputStream, extInputBuilder);
		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		threadPool.submit(inputConsumer);
		threadPool.submit(extInputConsumer);
		channel.connect();
		threadPool.shutdown();
		try {
			logger.debug("before waiting, exit status = " + channel.getExitStatus());
			boolean executorTerminated = threadPool.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
			logger.debug("after waiting, exit status = " + channel.getExitStatus() + "\tclosed? " + channel.isClosed()
					+ "\tconnected? " + channel.isConnected() + "\tEOF? " + channel.isEOF());
			if (!executorTerminated) {
				logger.debug("ssh command timed out after " + timeoutMillis + "ms");
				threadPool.shutdownNow();
			}
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		} finally {
			channel.disconnect();
		}
		logger.debug("done with command, exit status = " + channel.getExitStatus());
		IOException exception = inputConsumer.getException();
		if (exception != null) {
			throw exception;
		}
		exception = extInputConsumer.getException();
		if (exception != null) {
			throw exception;
		}

		SshResult result = new SshResult(inputBuilder.toString(), extInputBuilder.toString(), channel.getExitStatus());
		return result;
	}
}
