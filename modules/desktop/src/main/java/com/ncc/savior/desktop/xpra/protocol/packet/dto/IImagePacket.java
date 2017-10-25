package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;

/**
 * Any packet that contains an image to be decoded.
 *
 *
 */
public interface IImagePacket {

	ImageEncoding getEncoding();

	byte[] getData();

}
