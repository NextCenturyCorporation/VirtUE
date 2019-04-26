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
package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;

/**
 * Used for a single place where default connection options are set for SSH.
 *
 *
 */
public class JschUtils {
	private static final Logger logger = LoggerFactory.getLogger(JschUtils.class);

	/**
	 * Session needs to be connected by calling .connect()
	 *
	 * @param params
	 * @return
	 * @throws JSchException
	 *                           if the PEM key is invalid (see
	 *                           {@link SshConnectionParameters#getPemFile()} and
	 *                           {@link SshConnectionParameters#getPemString()})
	 */
	// TODO review parameters for sanity!
	public static Session getUnconnectedSession(SshConnectionParameters params) throws JSchException {
		JSch jsch = new JSch();
		com.jcraft.jsch.Logger sshLogger = new com.jcraft.jsch.Logger() {

			@Override
			public void log(int level, String message) {
				logger.debug("level:" + level + " : " + message);
			}

			@Override
			public boolean isEnabled(int level) {
				return false;
			}
		};
		JSch.setLogger(sshLogger);
		File tempPem = null;
		try {
			File pem = params.getPemFile();
			if (pem != null && pem.exists()) {
				jsch.addIdentity(pem.getAbsolutePath());
			} else if (params.getPemString() != null) {
				tempPem = File.createTempFile("savior-tmp-", ".pem");
				SshUtil.writeKeyToFile(tempPem, params.getPemString());
				jsch.addIdentity(tempPem.getAbsolutePath());
			} else {
				logger.warn("no identity included with SSH connection parameters");
			}
			Session session = jsch.getSession(params.getUser(), params.getHost(), params.getPort());
			session.setServerAliveInterval(1000);
			session.setServerAliveCountMax(15);
			session.setTimeout(1000);
			session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
			session.setPassword(params.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			return session;
		} catch (IOException e) {
			String msg = "Error attempting to connection with " + params;
			logger.error(msg);
			throw new SaviorException(SaviorErrorCode.SSH_ERROR, msg, e);
		} finally {
			// clear temporary file
			if (tempPem != null && tempPem.exists()) {
				boolean deleted = tempPem.delete();
				if (!deleted) {
					logger.warn("failed to delete temporary pem file at " + tempPem.getAbsolutePath());
				}
			}
		}
	}

}
