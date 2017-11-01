package com.ncc.savior.desktop.xpra.protocol.packet;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Test;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

public class PacketBuilderTest {

	@Test
	public void testBuildAllPackets() {
		for (PacketType type : PacketType.values()) {
			Constructor<? extends Packet> constructor = null;
			Class<? extends Packet> klass = null;
			try {
				klass = type.getPacketClass();
				if (klass != null) {
					constructor = klass.getConstructor(List.class);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				fail("No valid constructor found for type=" + type + ", Exception: " + e.getMessage());
			}
			if (klass != null && constructor == null) {
				fail("No valid constructor found for type=" + type);
			}

		}
	}
}
