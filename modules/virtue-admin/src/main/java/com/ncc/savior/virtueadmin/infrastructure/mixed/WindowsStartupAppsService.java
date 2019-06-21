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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;

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
	private IActiveVirtueDao activeVirtueDao;
	private Thread pollingThread;
	private IKeyManager keyManager;
	private List<String> virtueList;
	private String windowsStartupScript = "windowsStartup.tpl";
	private ITemplateService templateService;

	public WindowsStartupAppsService(IActiveVirtueDao activeVirtueDao, IKeyManager keyManager,
			ITemplateService templateService) {
		this.activeVirtueDao = activeVirtueDao;
		this.keyManager = keyManager;
		this.templateService = templateService;
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
			session = SshUtil.getConnectedSession(windows, keyFile);
			Map<String, Object> dataModel = new HashMap<String, Object>();
			dataModel.put("nfs", nfsOrXen);
			dataModel.put("vm", windows);
			SshUtil.runCommandsFromFileWithTimeout(templateService, session, windowsStartupScript, dataModel, 5000);
			// String cmd = String.format(command, nfsOrXen.getInternalIpAddress());
			// String cmd2 = String.format(command2, nfsOrXen.getInternalIpAddress());
			// List<String> output = SshUtil.sendCommandFromSession(session, cmd);
			// logger.debug("Cmd=" + cmd + " output=" + output.toString());
			// output = SshUtil.sendCommandFromSession(session, cmd2);
			// logger.debug(output.toString());
			return true;
		} catch (JSchException | IOException e) {
			logger.debug("mount failed", e);
		} catch (TemplateException e) {
			logger.error("error attempting to initiate windows startup services", e);
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

	public void setWindowsStartupScript(String windowsStartupScript) {
		this.windowsStartupScript = windowsStartupScript;
	}

}
