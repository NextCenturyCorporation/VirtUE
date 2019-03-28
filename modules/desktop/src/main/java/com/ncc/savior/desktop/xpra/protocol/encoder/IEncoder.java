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
package com.ncc.savior.desktop.xpra.protocol.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Xpra supports multiple encodings of the Packet data. The encodings are:
 * <ul>
 * <li>Bencode
 * <li>Rencode
 * </ul>
 *
 * Rencode is supposedly more performance than Bencode.
 * <p>
 * More encodings may be implemented in the future.
 *
 */
public interface IEncoder {
    List<Object> decode(InputStream input) throws IOException;

    void encode(ByteArrayOutputStream baos, List<Object> list) throws IOException;

    byte getProtocolFlags();
}
