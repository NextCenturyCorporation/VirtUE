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

	/**
	 * Allocate a new {@link Memory} object containing exactly the passed bytes.
	 * 
	 * @param bytes
	 *                  what to fill the memory object with
	 * @return a new, populated {@link Memory} object
	 */
	static public Memory newMemory(byte... bytes) {
		Memory memory = new Memory(bytes.length);
		memory.write(0, bytes, 0, bytes.length);
		return memory;
	}

}
