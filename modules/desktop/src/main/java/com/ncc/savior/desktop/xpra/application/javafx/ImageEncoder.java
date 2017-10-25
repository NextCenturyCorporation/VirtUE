package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;

import javafx.scene.image.Image;

/**
 * This class handles decoding images for the system. Additional encodings will
 * be implemented as needed.
 *
 *
 */
public class ImageEncoder {
	private static final Logger logger = LoggerFactory.getLogger(ImageEncoder.class);

	public static Image decodeImage(ImageEncoding encoding, byte[] data) {
		switch (encoding) {
		case png:
			Image img = new Image(new ByteArrayInputStream(data));
			return img;
		default:
			logger.error("Unable to decode image with encoding=" + encoding.getCode());
			return null;
		}
	}

}
