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
package com.ncc.savior.desktop.clipboard.serialization;

import java.io.Closeable;
import java.io.IOException;

import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Abstraction for class that serializes and deserializes
 * {@link IClipboardMessage}s and passes them on. Typically
 * {@link IMessageSerializer}s have an {@link IConnectionWrapper} to pass the
 * serialized messages on.
 *
 *
 */
public interface IMessageSerializer extends Closeable {
	/**
	 * Serializes the message and passes it on to an implementation specific stream.
	 * The stream should be flushed before returning. Therefore, this method could
	 * block if the underlying serialization or stream does not complete
	 * immediately.
	 *
	 * @param message
	 * @throws IOException
	 */
	public void serialize(IClipboardMessage message) throws IOException;

	/**
	 * Blocks and deserializes next message from implementation specific stream.
	 *
	 * @return
	 * @throws IOException
	 */
	public IClipboardMessage deserialize() throws IOException;

	public static IMessageSerializer getDefaultSerializer(IConnectionWrapper wrapper) throws IOException {
		// return new JsonObjectMessageSerializer(wrapper);
		return new JavaObjectMessageSerializer(wrapper);
	}
}
