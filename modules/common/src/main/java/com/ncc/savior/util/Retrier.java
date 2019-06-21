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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author clong
 *
 */
public class Retrier implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Retrier.class);

	private Runnable delegate;
	private int retries;
	private int delayMs;
	private Collection<Class<? extends Throwable>> retriable;
	volatile Boolean successful = null;

	public Retrier(Runnable delegate, int retries, int delayMs, Collection<Class<? extends Throwable>> retriable) {
		this.delegate = delegate;
		this.retries = retries;
		this.delayMs = delayMs;
		this.retriable = retriable;
	}

	@Override
	public void run() {
		for (int i = 0; i <= retries; i++) {
			try {
				delegate.run();
				break;
			} catch (Throwable t) {
				if (!retriable.stream().anyMatch(throwableClass -> throwableClass.isInstance(t))) {
					logger.warn("failed due to non-retriable exception: " + t);
					successful = false;
					break;
				}
				if (delayMs > 0) {
					try {
						Thread.sleep(delayMs);
					} catch (InterruptedException e) {
						logger.debug("stopping thread because it was interrupted");
						successful = false;
						break;
					}
				}
			}
		}
		if (successful == null) {
			successful = true;
		}
	}

	public Boolean isSuccessful() {
		return successful;
	}
}
