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
package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.concurrent.CompletableFuture;

/**
 * Simple Untity implementation of {@link BaseCompletableFutureService} for
 * fast, immediate tasks. Concrete implementations should implement
 * {@link #onExecute(Object, Object)} returning the resulting value. Throwing an
 * exception will cause a failure.
 * 
 *
 * @param <P>
 *            - input parameter to service that will be given from
 *            {@link CompletableFuture} (usually from previous service)
 * @param <R>
 *            - Return type parameter that is returned via return
 *            {@link CompletableFuture} (often goes to the next service)
 * @param <X>
 *            - extra information class. Can be any class and is just passed
 *            along with the data. This could be a virtue id, for example.
 */
public abstract class BaseImediateCompletableFutureService<P, R, X> extends BaseCompletableFutureService<P, R, X> {

	private String serviceName = null;

	public BaseImediateCompletableFutureService(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	protected void offer(P p, X extra, CompletableFuture<R> cf) {
		try {
			R result = onExecute(p, extra);
			onSuccess(result, cf);
		} catch (Exception e) {
			onFailure(p, e, cf);
		}
	}

	protected abstract R onExecute(P param, X extra);

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return serviceName;
	}

}
