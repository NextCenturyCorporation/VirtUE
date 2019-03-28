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
package com.ncc.savior.desktop.alerting;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * This class is a placeholder for an alerting system. For now it will just help
 * us remember where we want to do user alerts. This implementation will just
 * print to the local logger, but will eventually be replaced with an entire
 * system.
 *
 *
 */

// TODO worry about synchronization??
public class UserAlertingServiceHolder {
	// private static final Logger logger =
	// LoggerFactory.getLogger(UserAlertingServiceHolder.class);
	private static IUserAlertService alertService;
	private static IAlertHistoryManager alertHistoryManager;

	public static void sendAlert(BaseAlertMessage alertMessage) throws IOException {
		if (alertService == null) {
			alertService = new LoggingAlertService();
		}

		if (alertHistoryManager == null) {
			alertHistoryManager = new AlertHistoryWriter();
		}

		alertService.displayAlert(alertMessage);

		alertHistoryManager.storeAlert(alertMessage);
	}

	public static void sendAlertLogError(BaseAlertMessage alertMessage, Logger logger) {
		try {
			sendAlert(alertMessage);
		} catch (IOException e) {
			logger.error("Failed to send alert message: " + alertMessage);
		}
	}

	public static void setAlertService(IUserAlertService alertService) {
		UserAlertingServiceHolder.alertService = alertService;
	}

	public static void resetHistoryManager() throws IOException {
		alertHistoryManager = new AlertHistoryWriter();
	}
}
