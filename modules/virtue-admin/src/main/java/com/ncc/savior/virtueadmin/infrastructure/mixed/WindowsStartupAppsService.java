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

/**
 * Used to add scripts, shortcuts or programs into the startup folder on a
 * windows VM such that those programs will start when logged in.
 * 
 * Typically, users of this class will call
 * {@link #addVirtueToQueue(VirtueInstance)} and this service will handle
 * waiting until the VM's in that virtue are ready. Once they are ready, the
 * scripts,etc will be added to the startup folder.
 * 
 * This class is a temporary patch until we use services.
 * 
 *
 */
public class WindowsStartupAppsService {
	private static final Logger logger = LoggerFactory.getLogger(WindowsStartupAppsService.class);
	private String command1File = "\"AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\mountNfs.bat\"";
	private String command = "echo mount -o mtype=hard %s:/disk/nfs t: > " + command1File;
	private String command2File = "\"AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\startSensors.bat\"";
	private String command2 = "echo C:\\Users\\Administrator\\savior\\bin\\run-all.bat > " + command2File;
	private IActiveVirtueDao activeVirtueDao;
	private Thread pollingThread;
	private IKeyManager keyManager;
	private List<String> virtueList;

	public WindowsStartupAppsService(IActiveVirtueDao activeVirtueDao, IKeyManager keyManager) {
		this.activeVirtueDao = activeVirtueDao;
		this.keyManager = keyManager;
		this.virtueList = Collections.synchronizedList(new ArrayList<String>());
		// startPollingThread();
	}

	public void startPollingThread() {
		this.pollingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					pollVirtues();
				} catch (Throwable t) {
					logger.error("Windows NFS Mounting Service polling thread stopped!", t);
				}
			}
		}, "WindowsStartupServiceThread");
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
							boolean mySuccess = addWindowsStartupServices(xenVm.get(), vm);
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

	public boolean addWindowsStartupServices(VirtualMachine nfsOrXen, VirtualMachine windows) {
		Session session = null;
		logger.debug("Attempting to mount NFS on windows box for virtue " + nfsOrXen.getId());
		try {
			File keyFile = keyManager.getKeyFileByName(windows.getPrivateKeyName());
			session=SshUtil.getConnectedSession(windows, keyFile);
			String cmd = String.format(command, nfsOrXen.getInternalIpAddress());
			String cmd2 = String.format(command2, nfsOrXen.getInternalIpAddress());
			List<String> output = SshUtil.sendCommandFromSession(session, cmd);
			logger.debug("Cmd=" + cmd + " output=" + output.toString());
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

	/**
	 * Adds the given virtue to the list of Virtues. This service will apply the
	 * startup commands when the appropriate VM's are ready.
	 * 
	 * @param vi
	 */
	public void addVirtueToQueue(VirtueInstance vi) {
		virtueList.add(vi.getId());
	}

}
