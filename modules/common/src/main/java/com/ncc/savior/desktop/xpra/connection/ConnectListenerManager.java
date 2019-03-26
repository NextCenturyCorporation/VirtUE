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
import java.util.ArrayList;
import java.util.List;

public class ConnectListenerManager implements IConnectListener {
	protected final List<IConnectListener> listeners = new ArrayList<>();

	public void addListener(IConnectListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public boolean removeListener(IConnectListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	@Override
	public void onBeforeConnectAttempt(IConnectionParameters parameters) {
		synchronized (listeners) {
			for (IConnectListener l : listeners) {
				l.onBeforeConnectAttempt(parameters);
			}
		}
	}

	@Override
	public void onConnectSuccess(IConnection conn) {
		synchronized (listeners) {
			for (IConnectListener l : listeners) {
				l.onConnectSuccess(conn);
			}
		}
	}

	@Override
	public void onConnectFailure(IConnectionParameters params, IOException e) {
		synchronized (listeners) {
			for (IConnectListener l : listeners) {
				l.onConnectFailure(params, e);
			}
		}
	}
}
