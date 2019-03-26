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

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * A component of an {@link IUpdatePipeline}. Each pipeline does an action on
 * VMs and retries until it succeeds.
 * 
 * Component instances cannot be reused between different pipelines!
 *
 */
public interface IPipelineComponent<T> {

	/**
	 * Adds {@link VirtualMachine}s to this particular {@link IPipelineComponent}.
	 * 
	 * @param vms
	 */
	void addPipelineElements(Collection<PipelineWrapper<T>> vms);

	/**
	 * Called when an {@link IUpdatePipeline} is started so
	 * {@link IPipelineComponent}s can perform initial startup actions and store
	 * their index. The index is used later in callbacks.
	 * 
	 * @param index
	 */
	void onPipelineStart(int index);

	/**
	 * Store a listener to indicate when VM's have succeeded or failed and should
	 * either progress through the pipeline or be removed from the pipeline.
	 * 
	 * @param updatePipeline
	 */
	void setResultListener(IUpdatePipelineResultListener<T> updatePipeline);

}
