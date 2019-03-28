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
package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.HashMap;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

public class BufferRefreshPacket extends WindowPacket {

	private int unused;
	private int quality;
	private HashMap<String, String> refreshOptions;
	private HashMap<String, String> clientProperties;

	public BufferRefreshPacket(int windowId) {
		super(windowId, PacketType.BUFFER_REFRESH);
		this.unused = 0;
		this.quality = 100;
		this.refreshOptions = new HashMap<String, String>();
		this.clientProperties = new HashMap<String, String>();
	}

	public BufferRefreshPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));

	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(unused);
		list.add(quality);
		list.add(refreshOptions);
		list.add(clientProperties);
	}

	public int getUnused() {
		return unused;
	}

	public int getQuality() {
		return quality;
	}

	public HashMap<String, String> getRefreshOptions() {
		return refreshOptions;
	}

	public HashMap<String, String> getClientProperties() {
		return clientProperties;
	}
}
