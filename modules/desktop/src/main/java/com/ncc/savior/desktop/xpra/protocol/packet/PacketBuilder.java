package com.ncc.savior.desktop.xpra.protocol.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.UnknownPacket;

/**
 * Builds packets based on their constructed list of {@link Object}. The first
 * Object is always a type descriptor which is matched to the {@link PacketType}
 * enum. If the enum has an attached class, it will construct it to the specific
 * {@link Packet}, otherwise it will create an {@link UnknownPacket}.
 *
 *
 */
public class PacketBuilder {
	private static final Logger logger = LoggerFactory.getLogger(PacketBuilder.class);

	public Packet buildPacket(List<Object> list) {
		if (list.isEmpty()) {
			logger.error("Received packed that was too small.  Size=0");
		}
		String typeStr = PacketUtils.asString(list.get(0));
		PacketType type = PacketType.getPacketType(typeStr);
		if (type != null) {
			try {
				Class<? extends Packet> klass = type.getPacketClass();
				if (klass != null) {
					Constructor<? extends Packet> constructor = type.getPacketClass().getConstructor(List.class);
					return (Packet) constructor.newInstance(list);
				} else {
					logger.warn("Received packet without implemented class=" + type);
				}
			} catch (NoSuchMethodException e) {
				logger.warn("Unable to handle building packet", e);
			} catch (IllegalAccessException e) {
				logger.warn("Unable to handle building packet", e);
			} catch (InstantiationException e) {
				logger.warn("Unable to handle building packet", e);
			} catch (InvocationTargetException e) {
				logger.warn("Unable to handle building packet", e);
			}
		} else {
			logger.warn("Unable to handle building packet.  No type for String=" + typeStr);
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				if (o instanceof byte[]) {
					logger.debug("param" + i + ": " + o + " / " + new String((byte[]) o));
				} else {
					logger.debug("param" + i + ": " + o);
				}
			}
		}
		return new UnknownPacket(list);
	}
}
