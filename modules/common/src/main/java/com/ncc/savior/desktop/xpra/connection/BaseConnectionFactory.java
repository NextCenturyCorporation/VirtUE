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
package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseConnectionFactory {
    private static Logger logger = LoggerFactory.getLogger(BaseConnectionFactory.class);


	public IConnection connect(IConnectionParameters params, ConnectListenerManager listenerManager) {
        try {
			listenerManager.onBeforeConnectAttempt(params);
        } catch (Exception e) {
            logger.warn("Error attempting to send beforeConnectionAttempt event!", e);
        }
        IConnection connection = null;
        try {
            connection = doConnect(params);
        } catch (IOException e) {
            try {
				listenerManager.onConnectFailure(params, e);
            } catch (Exception e2) {
                logger.warn("Error attempting to send onConnectionFailure event!", e2);
            }
        }

        try {
			listenerManager.onConnectSuccess(connection);
        } catch (Exception e) {
            logger.warn("Error attempting to send onConnectionSuccess event!", e);
        }
        return connection;
    }

    protected abstract IConnection doConnect(IConnectionParameters params) throws IOException;

	// public abstract int getDisplay2();
}
