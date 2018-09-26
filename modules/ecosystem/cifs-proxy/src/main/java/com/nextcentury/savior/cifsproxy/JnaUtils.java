package com.nextcentury.savior.cifsproxy;

import com.sun.jna.Memory;

/**
 * Convenience methods for dealing with JNA.
 * 
 * @author clong
 *
 */
public class JnaUtils {

	private JnaUtils() {
	}

	static public Memory newMemory(String value) {
		return newMemory(value, value.length() + 1);
	}

	static public Memory newMemory(String value, int length) {
		Memory memory = new Memory(length);
		memory.setString(0, value);
		return memory;
	}

	static public Memory newMemory(byte... bytes) {
		Memory memory = new Memory(bytes.length);
		memory.write(0, bytes, 0, bytes.length);
		return memory;
	}

}
