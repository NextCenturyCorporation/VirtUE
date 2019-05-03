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
package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.SimpleApplicationManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;

/**
 * Component of an {@link IUpdatePipeline} that will start Xpra on the
 * {@link VirtualMachine}. It will use
 * {@link VirtualMachine#getPrivateKeyName()} and a passin {@link IKeyManager}
 * to login.
 */
public class StartXpraComponent extends BaseIndividualVmPipelineComponent<VirtualMachine> {
	private static final Logger logger = LoggerFactory.getLogger(StartXpraComponent.class);
	private IKeyManager keyManager;
	private SimpleApplicationManager appManager;

	public StartXpraComponent(ScheduledExecutorService executor, IKeyManager keyManager, ITemplateService templateService) {
		super(executor, false, 100, 1500);
		this.keyManager = keyManager;
		this.appManager = new SimpleApplicationManager(templateService);
	}

	@Override
	protected void onExecute(PipelineWrapper<VirtualMachine> wrapper) {
		attemptStartXpra(wrapper);

	}

	protected void attemptStartXpra(PipelineWrapper<VirtualMachine> wrapper) {
		VirtualMachine vm = wrapper.get();
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("starting xpra get " + vm);
			}
			int display = appManager.startOrGetXpraServer(vm, privateKeyFile);
			if (display > 0) {
				vm.setState(VmState.RUNNING);
				doOnSuccess(wrapper);
			}
		} catch (IOException e) {
			logger.debug("Failed to start XPRA", e);
		}
	}

	@Override
	protected String getId(VirtualMachine element) {
		return element.getId();
	}
}
