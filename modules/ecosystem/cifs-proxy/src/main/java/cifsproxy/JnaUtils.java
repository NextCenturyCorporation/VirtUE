package cifsproxy;

import com.sun.jna.Memory;

public class JnaUtils {

	private JnaUtils() {
	}

	static Memory newMemory(String value) {
		return newMemory(value, value.length() + 1);
	}
	
	static Memory newMemory(String value, int length) {
		Memory memory = new Memory(length);
		memory.setString(0, value);
		return memory;
	}
	
	static Memory newMemory(byte... bytes) {
		Memory memory = new Memory(bytes.length);
		memory.write(0, bytes, 0, bytes.length);
		return memory;
	}
	
}
