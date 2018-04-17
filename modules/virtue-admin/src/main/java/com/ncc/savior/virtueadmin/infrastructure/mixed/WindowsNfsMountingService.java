package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;
import com.ncc.savior.virtueadmin.util.SshUtil;

public class WindowsNfsMountingService {
	private static final Logger logger = LoggerFactory.getLogger(WindowsNfsMountingService.class);
	private String command1File = "\"AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\mountNfs.bat\"";
	private String command = "echo mount -o mtype=hard %s:/disk/nfs t: > " + command1File;
	private String command2File = "\"AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\startSensors.bat\"";
	private String command2 = "echo C:\\Users\\Administrator\\savior\\bin\\run-all.bat > " + command2File;
	private IActiveVirtueDao activeVirtueDao;
	private Thread pollingThread;
	private IKeyManager keyManager;
	private List<String> virtueList;

	public WindowsNfsMountingService(IActiveVirtueDao activeVirtueDao, IKeyManager keyManager) {
		this.activeVirtueDao = activeVirtueDao;
		this.keyManager = keyManager;
		this.virtueList = Collections.synchronizedList(new ArrayList<String>());
		startPollingThread();
	}

	private void startPollingThread() {
		this.pollingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					pollVirtues();
				} catch (Throwable t) {
					logger.error("Windows NFS Mounting Service polling thread stopped!", t);
				}
			}
		}, "WindowsNfsMountingService");
		this.pollingThread.setDaemon(true);
		this.pollingThread.start();
	}

	protected void pollVirtues() {
		while (true) {
			try {
				Iterable<VirtueInstance> virtues = activeVirtueDao.getVirtueInstances(virtueList);
				Iterator<VirtueInstance> itr = virtues.iterator();
				while (itr.hasNext()) {
					VirtueInstance v = itr.next();
					boolean success = testAndMountVirtue(v);
					if (success) {
						virtueList.remove(v.getId());
					}
				}
				JavaUtil.sleepAndLogInterruption(2000);
			} catch (Exception e) {

			}
		}
	}

	private boolean testAndMountVirtue(VirtueInstance v) {
		try {
			Optional<VirtualMachine> xenVm = activeVirtueDao.getXenVm(v.getId());
			if (xenVm.isPresent() && VmState.RUNNING.equals(xenVm.get().getState())) {
				// if xen box is running
				if (VirtueState.RUNNING.equals(v.getState())) {
					// if all boxes are running
					boolean success = true;
					for (VirtualMachine vm : v.getVms()) {
						// go through each VM for windwos boxes
						if (OS.WINDOWS.equals(vm.getOs())) {
							boolean mySuccess = addNfsMountToStartup(xenVm.get(), vm);
							success &= mySuccess;
						}
					}
					return success;
				}
			}
		} catch (Exception e) {
			logger.error("Error testing and mounting virtue", e);
		}
		return false;
	}

	public boolean addNfsMountToStartup(VirtualMachine nfs, VirtualMachine windows) {
		JSch ssh = new JSch();
		Session session = null;
		logger.debug("Attempting to mount NFS on windows box for virtue " + nfs.getId());
		try {
			File keyFile = keyManager.getKeyFileByName(windows.getPrivateKeyName());
			ssh.addIdentity(keyFile.getAbsolutePath());
			session = ssh.getSession(windows.getUserName(), windows.getHostname(), 22);
			session.setConfig("PreferredAuthentications", "publickey");
			session.setConfig("StrictHostKeyChecking", "no");
			session.setTimeout(500);
			session.connect();
			String cmd = String.format(command, nfs.getInternalIpAddress());
			String cmd2 = String.format(command2, nfs.getInternalIpAddress());
			List<String> output = SshUtil.sendCommandFromSession(session, cmd);
			logger.debug(output.toString());
			output = SshUtil.sendCommandFromSession(session, cmd2);
			logger.debug(output.toString());
			return true;
		} catch (JSchException e) {
			logger.debug("mount failed", e);
		} catch (IOException e) {
			logger.debug("mount failed", e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
		return false;
	}

	public void mountNfsOnWindowsBoxes(VirtueInstance vi) {
		virtueList.add(vi.getId());
	}

}
