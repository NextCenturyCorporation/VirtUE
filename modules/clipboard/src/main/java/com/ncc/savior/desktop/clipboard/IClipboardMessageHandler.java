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

import java.io.IOException;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Interface to handle {@link IClipboardMessage}s
 *
 *
 */
public interface IClipboardMessageHandler {
	/**
	 * Called when an {@link IClipboardMessage} is received
	 *
	 * @param message
	 * @param groupId
	 */
	public void onMessage(IClipboardMessage message, String groupId);

	/**
	 * Called when there is an error trying to send or receive a clipboard message.
	 * 
	 * @param description
	 *
	 * @param e
	 */
	public void onMessageError(String description, IOException e);

	/**
	 * called with transmitter was closed.
	 */
	public void closed();

}
