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
package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.ncc.savior.desktop.clipboard.client.StandardInOutClipboardClient;

/**
 * Implementation of {@link IConnectionWrapper} that uses a {@link JSch}
 * {@link Channel}. These are often created via connecting to a remote machine
 * via Jsch's implementation of SSH and are used with
 * {@link StandardInOutClipboardClient}s and {@link StandardInOutConnection} on
 * the remote machine.
 * 
 *
 */
public class JschChannelConnectionWrapper implements IConnectionWrapper {

	private Channel channel;

	public JschChannelConnectionWrapper(Channel ch) {
		this.channel = ch;
	}

	@Override
	public void close() throws IOException {
		if (channel != null) {
			channel.disconnect();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return channel.getOutputStream();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return channel.getInputStream();
	}

}
