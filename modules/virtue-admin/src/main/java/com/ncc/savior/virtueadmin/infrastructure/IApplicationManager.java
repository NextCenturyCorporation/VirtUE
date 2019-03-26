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
package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.io.IOException;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Starts and manages applications running on VMs.
 * 
 *
 */
public interface IApplicationManager {
	/**
	 * Initiates an application on a given VM.
	 * 
	 * @param vm
	 * @param application
	 * @param params
	 */
	void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition application, String params, int maxTries);

	/**
	 * Tries to get a started Xpra server. If none is found, it will attempt to
	 * start one and immediately test for it.
	 * 
	 * -1 means no server
	 * 
	 * throwing means failure.
	 * 
	 */
	int startOrGetXpraServer(VirtualMachine vm, File privateKeyFile) throws IOException;

	int startOrGetXpraServerWithRetries(VirtualMachine vm, File privateKeyFile, int maxTries);
}
