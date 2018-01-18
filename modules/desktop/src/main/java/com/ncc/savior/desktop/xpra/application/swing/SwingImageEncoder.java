package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;


/**
 * This class handles decoding images for the system. Additional encodings will
 * be implemented as needed.
 *
 *
 */
public class SwingImageEncoder {
	private static final Logger logger = LoggerFactory.getLogger(SwingImageEncoder.class);

	public static Image decodeImage(ImageEncoding encoding, byte[] data) {
		switch (encoding) {
		case png:
			Image img = null;
			try {
				img = ImageIO.read(new ByteArrayInputStream(data));
			} catch (IOException e) {
				logger.warn("Exception reading image", e);
			}
			return img;
		case premult_argb32:
			// Assume square
			int width = (int) Math.sqrt(data.length / 4);
			// This is not efficient. Hopefully its only used for an occasional icon.
			BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
			DataBuffer buffer = (image.getRaster().getDataBuffer());
			int[] array = ((DataBufferInt) buffer).getData();
			for (int i = 0; i < data.length; i = i + 4) {
				byte b4 = data[i];
				byte b3 = data[i + 1];
				byte b2 = data[i + 2];
				byte b1 = data[i + 3];
				int val = ((0xFF & b1) << 24) | ((0xFF & b2) << 16) | ((0xFF & b3) << 8) | (0xFF & b4);
				array[i / 4] = val;
			}
			// image = new BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR_PRE);
			// ColorModel cm = new
			// ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false,
			// Transparency.TRANSLUCENT, DataBuffer.TYPE_INT);
			// image = new BufferedImage(cm, image.getRaster(), true, null);
			return image;

		default:
			logger.error("Unable to decode image with encoding=" + encoding.getCode());
			return null;
		}
	}

}