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
package com.ncc.savior.desktop.xpra.protocol.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions for appropriately reading packet data and converting to the
 * right data type.
 *
 *
 */
public class PacketUtils {
	private static final Logger logger = LoggerFactory.getLogger(PacketUtils.class);

	public static boolean asBoolean(Object obj) {
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		} else if (obj instanceof Number) {
			return ((Number) obj).intValue() != 0;
		}
		return false;
	}

	public static int asInt(Object obj) {
		return ((Number) obj).intValue();
	}

	public static long asLong(Object obj) {
		return ((Number) obj).longValue();
	}

	public static String asString(Object obj) {
		if (obj instanceof byte[]) {
			return new String((byte[]) obj);
		} else {
			return (String) obj;
		}
	}

	public byte[] asByteArray(Object obj) {
		return (byte[]) obj;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> asStringObjectMap(Object obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> origMap = (Map<String, Object>) obj;
		for (Entry<String, Object> entry : origMap.entrySet()) {
			Object value = entry.getValue();
			value = convertByteArraysToStrings(value);
			map.put(entry.getKey(), value);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static Object convertByteArraysToStrings(Object value) {
		if (value instanceof byte[]) {
			value = new String((byte[]) value);
		} else if (value instanceof Map) {
			value = asStringObjectMap(value);
		} else if (value instanceof List) {
			value = convertByteArraysToStrings((List<Object>) value);
		}
		return value;
	}

	public static List<Object> convertByteArraysToStrings(List<Object> oldList) {
		List<Object> list = new ArrayList<Object>();
		for (Object o : oldList) {
			Object v = convertByteArraysToStrings(o);
			list.add(v);
		}
		return list;
	}

	public static Map<String, Object> asStringObjectMap(List<Object> list, int index) {
		if (list.size() > index) {
			Object obj = list.get(index);
			if (obj instanceof Map) {
				return (obj == null ? null : asStringObjectMap(obj));
			}
		}
		return null;
	}

	public static int asInt(List<Object> list, int i) {
		return asInt(list.get(i));
	}

	public static long asLong(List<Object> list, int i) {
		return asLong(list.get(i));
	}

	@SuppressWarnings("unchecked")
	public static List<String> asStringList(Object obj) {
		if (obj instanceof List) {
			return (obj == null ? null : (List<String>) obj);
		}
		return null;
	}
}
