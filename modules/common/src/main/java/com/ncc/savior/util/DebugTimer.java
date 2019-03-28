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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugTimer {
	private static final Logger logger = LoggerFactory.getLogger(DebugTimer.class);
	private String name;
	private long startMillis;
	private long lastInterval;
	private ArrayList<String> intervalResults;

	public DebugTimer(String name) {
		this.intervalResults = new ArrayList<String>(15);
		this.name = name;
		this.startMillis = System.currentTimeMillis();
		this.lastInterval = startMillis;
	}

	public void stop() {
		long now = System.currentTimeMillis();
		interval("end");
		long elapsed = now - startMillis;
		logger.debug("Timer " + name + " : " + getPrettyTime(elapsed));
		if (intervalResults.size() > 1) {
			for (String result : intervalResults) {
				logger.debug("  " + result);
			}
		}
	}

	private String getPrettyTime(long elapsed) {
		return elapsed / 1000.0 + "s";
	}

	public void interval(String string) {
		long now = System.currentTimeMillis();
		long elapsed = now - lastInterval;
		lastInterval = now;
		intervalResults.add(string + " " + getPrettyTime(elapsed));

	}
}
