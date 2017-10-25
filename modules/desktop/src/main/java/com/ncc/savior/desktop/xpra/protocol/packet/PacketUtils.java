package com.ncc.savior.desktop.xpra.protocol.packet;

import java.util.List;
import java.util.Map;

/**
 * Utility functions for appropriately reading packet data and converting to the
 * right data type.
 *
 *
 */
public class PacketUtils {
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
		return (Map<String, Object>) obj;
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
}
