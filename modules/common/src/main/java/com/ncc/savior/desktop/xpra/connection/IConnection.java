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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Single connection to an Xpra server.
 *
 *
 */
public interface IConnection {

	/**
	 * Connection parameters used to create this connection. This can be used to
	 * reconnect or just for informational purposes.
	 *
	 * @return
	 */
	public IConnectionParameters getConnectionParameters();

	public InputStream getInputStream() throws IOException;

	public OutputStream getOutputStream() throws IOException;

	public boolean isActive();
}
