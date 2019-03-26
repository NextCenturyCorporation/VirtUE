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
package com.ncc.savior.desktop.clipboard;

import java.io.Closeable;
import java.io.IOException;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public interface IClipboardMessageSenderReceiver extends Closeable {

	/**
	 * Should be called initially and returns the ID
	 *
	 * @return
	 * @throws IOException
	 */
	String init() throws IOException;

	/**
	 * Sends a message back to the hub
	 *
	 * @param message
	 * @throws IOException
	 */
	void sendMessageToHub(IClipboardMessage message) throws IOException;

	/**
	 * Returns true as long as the instance hasn't throw an error on transmission.
	 * Transmission errors are assumed to be fatal.
	 *
	 * @return
	 */
	public boolean isValid();

	/**
	 * ID used to determine whether data should pass between the two endpoints.
	 *
	 * @return
	 */
	String getGroupId();

	/**
	 * wait until transmitter is stopped and no longer valid.
	 */
	void waitUntilStopped();
}
