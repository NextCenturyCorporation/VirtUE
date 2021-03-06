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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation on Bencode encoding/decoding.
 *
 *
 */
public class Bencoder {
	private static final Logger logger = LoggerFactory.getLogger(Bencoder.class);
	private static final char PREFIX_INT = 'i';
	private static final char SUFIX_NEGATIVE = '-';
	private static final char POSTFIX_NONSTR = 'e';
	private static final char DELEMETER_STR = ':';
	private static final char PREFIX_ARR = 'l';
	private static final char PREFIX_DIC = 'd';

	public void encodeList(List<Object> list, OutputStream os) throws ClassNotFoundException, IOException {
		Iterator<Object> itr = list.iterator();
		os.write(PREFIX_ARR);
		while (itr.hasNext()) {
			encodeObject(itr.next(), os);
		}
		os.write(POSTFIX_NONSTR);
	}

	public Object decodeStream(InputStream bis) throws IOException {
		Object obj = decodeObject(bis);
		return obj;
	}

	public void encodeObject(Object obj, OutputStream bos) {

		try {
			if (obj instanceof Boolean) {
				obj = ((Boolean) obj ? 1 : 0);
			}
			if (obj instanceof String) {
				String s = (String) obj;
				bos.write(String.valueOf(s.length()).getBytes());
				bos.write(DELEMETER_STR);
				bos.write(s.getBytes());
			} else if (obj instanceof Long) {
				Long l = (Long) obj;
				bos.write(PREFIX_INT);
				bos.write(l.toString().getBytes());
				bos.write(POSTFIX_NONSTR);
			} else if (obj instanceof Integer) {
				Long i = (Long) ((long) (int) obj);
				bos.write(PREFIX_INT);
				bos.write(i.toString().getBytes());
				bos.write(POSTFIX_NONSTR);
			} else if (obj instanceof List) {
				encodeCollection(obj, bos);
			} else if (obj instanceof Object[]) {
				encodeArray(obj, bos);
			} else if (obj instanceof int[]) {
				encodeIntArray(obj, bos);
			} else if (obj instanceof Map) {
				encodeMap(obj, bos);
			} else {
				throw new IllegalArgumentException(
						"The type of the encodable object isn't Bencodable: " + obj.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void encodeIntArray(Object obj, OutputStream bos) {
		try {
			bos.write(PREFIX_ARR);
			int[] list = (int[]) obj;
			for (Object o : list) {
				encodeObject(o, bos);
			}
			bos.write(POSTFIX_NONSTR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void encodeArray(Object obj, OutputStream bos) {
		try {
			bos.write(PREFIX_ARR);
			Object[] list = (Object[]) obj;
			for (Object o : list) {
				encodeObject(o, bos);
			}
			bos.write(POSTFIX_NONSTR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void encodeCollection(Object obj, OutputStream bos) throws IOException {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) obj;
		if (list.isEmpty()) {
			bos.write(PREFIX_ARR);
		} else {
			Object o = list.get(0);
			if (o instanceof Entry) {
				bos.write(PREFIX_DIC);
				@SuppressWarnings("unchecked")
				List<Entry<Object, Object>> dict = (List<Entry<Object, Object>>) obj;
				dict.stream().forEach(e -> encodeDictEntry(e, bos));
			} else {
				bos.write(PREFIX_ARR);
				list.stream().forEach(e -> encodeObject(e, bos));
			}
		}

		bos.write(POSTFIX_NONSTR);
	}

	public void encodeMap(Object obj, OutputStream bos) throws IOException {
		@SuppressWarnings("unchecked")
		Map<String, Object> list = (Map<String, Object>) obj;

		bos.write(PREFIX_DIC);

		for (Map.Entry<String, Object> entry : list.entrySet()) {
			encodeMapEntry(entry, bos);
		}

		bos.write(POSTFIX_NONSTR);
	}

	private void encodeMapEntry(Entry<String, Object> e, OutputStream bos) {
		if (e.getKey() instanceof String) {
			encodeObject(e.getKey(), bos);
			encodeObject(e.getValue(), bos);
		} else {
			throw new IllegalArgumentException("Bencoded dictionary key is not String type " + e.getKey());
		}
	}

	public void encodeDictEntry(Entry<Object, Object> e, OutputStream bos) {
		if (e.getKey() instanceof String) {
			encodeObject(e.getKey(), bos);
			encodeObject(e.getValue(), bos);
		} else {
			throw new IllegalArgumentException("Bencoded dictionary key is not String type " + e.getKey());
		}
	}

	public Object decodeObject(InputStream bis) throws IOException {
		char ch = (char) bis.read();
		if (Character.isDigit(ch)) {
			int length = decodeStringLength(ch, bis);
			byte[] b = decodeByte(bis, length);
			// String s = new String(b);
			// if (Arrays.equals(s.getBytes(), b)) {
			// return s;
			// } else {
			return b;
			// }
			// return b;
		} else if (ch == PREFIX_INT) {
			Long i = decodeLong(bis);
			return i;
		} else if (ch == PREFIX_ARR) {
			List<Object> array = decodeList(bis);
			return array;
		} else if (ch == PREFIX_DIC) {
			Map<String, Object> dict = decodeDict(bis);
			return dict;
		} else if ((byte) ch == -1) {
			// successful stream end
			return null;
		} else {
			throw new IOException("Object had wrong character. Character=" + ch + "BytesLeft=" + bis.available());
		}
	}

	private byte[] decodeByte(InputStream bis, int length) throws IOException {
		byte[] value = new byte[length];
		int b;
		for (int i = 0; i < length; i++) {
			b = bis.read();
			if (b == -1) {
				throw new IOException("Stream ended");
			} else {
				value[i] = (byte) b;
			}
		}
		return value;
	}

	private int decodeStringLength(char startedFrom, InputStream bis) throws IOException {
		int i = Character.getNumericValue(startedFrom);
		char ch;
		while (true) {
			ch = (char) bis.read();
			if (ch == DELEMETER_STR) {
				break;
			} else if ((byte) ch == -1) {
				throw new IOException("Stream ended");
			} else if (!Character.isDigit(ch)) {
				throw new IOException("String had wrong character. Character=" + ch + "BytesLeft=" + bis.available());
			} else {
				i = i * 10 + Character.getNumericValue(ch);
			}
		}
		return i;
	}

	private Long decodeLong(InputStream bis) throws IOException {
		int neg = 1;
		long i = 0;
		char ch;
		while (true) {
			ch = (char) bis.read();
			if (Character.isDigit(ch)) {
				i = i * 10 + Character.getNumericValue(ch);
			} else if (ch == SUFIX_NEGATIVE && i == 0) {
				neg = neg * -1;
			} else if (ch == POSTFIX_NONSTR) {
				break;
			} else if ((byte) ch == -1) {
				throw new IOException("Stream ended");
			} else {
				throw new IOException("Integer had wrong character. Character=" + ch + "BytesLeft=" + bis.available());
			}
		}
		return new Long(i * neg);
	}

	private List<Object> decodeList(InputStream bis) throws IOException {
		List<Object> list = new ArrayList<Object>();
		char ch;
		while (true) {
			ch = (char) bis.read();
			if (Character.isDigit(ch)) {
				int length = decodeStringLength(ch, bis);
				byte[] b = decodeByte(bis, length);
				// String s = new String(b);
				// if (Arrays.equals(s.getBytes(), b)) {
				list.add(b);
				// } else {
				// logger.debug("byte: " + b.length + " string: " + s.getBytes().length);
				// list.add(b);
				// }
			} else if (ch == PREFIX_INT) {
				Long i = decodeLong(bis);
				list.add(i);
			} else if (ch == PREFIX_ARR) {
				list.add(decodeList(bis));
			} else if (ch == PREFIX_DIC) {
				list.add(decodeDict(bis));
			} else if (ch == POSTFIX_NONSTR) {
				break;
			} else if ((byte) ch == -1) {
				throw new IOException("Stream ended.");
			} else {
				throw new IOException("List had wrong character. Character=" + ch + "BytesLeft=" + bis.available());
			}
		}
		return list;
	}

	private Map<String, Object> decodeDict(InputStream bis) throws IOException {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		char ch;
		while (true) {
			ch = (char) bis.read();
			if (Character.isDigit(ch)) {
				int length = decodeStringLength(ch, bis);
				byte[] s = decodeByte(bis, length);
				Object o = decodeObject(bis);
				map.put(new String(s), o);
			} else if (ch == POSTFIX_NONSTR) {
				break;
			} else if ((byte) ch == -1) {
				throw new IOException("Stream ended.");
			} else {
				throw new IOException(
						"Dictionary had wrong character. Character=" + ch + "BytesLeft=" + bis.available());
			}
		}
		return map;
	}
}
