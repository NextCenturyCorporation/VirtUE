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
package com.ncc.savior.desktop.xpra.application.swing;

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

	public static BufferedImage decodeImage(ImageEncoding encoding, byte[] data, int width, int height) {
		BufferedImage image = null;
		try {
			DataBuffer buffer;
			int[] array;
			// logger.debug("decoding image: " + encoding.toString());
			switch (encoding) {
			case png:
			case jpeg:
				try {
					image = ImageIO.read(new ByteArrayInputStream(data));
				} catch (IOException e) {
					logger.warn("Exception reading image", e);
				}
				break;
			case rgb24:
				// This is not efficient. Hopefully its only used for an occasional icon.
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				buffer = image.getRaster().getDataBuffer();
				array = ((DataBufferInt) buffer).getData();
				for (int i = 0; i + 2 < data.length; i = i + 3) {
					byte b2 = data[i];
					byte b3 = data[i + 1];
					byte b4 = data[i + 2];
					byte b1 = 0;
					int val = ((0xFF & b1) << 24) | ((0xFF & b2) << 16) | ((0xFF & b3) << 8) | (0xFF & b4);
					array[i / 3] = val;
				}
				break;
			case premult_argb32:
				// Assume square

				// This is not efficient. Hopefully its only used for an occasional icon.
				image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
				buffer = image.getRaster().getDataBuffer();
				array = ((DataBufferInt) buffer).getData();
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
				break;
			default:
				logger.error("Unable to decode image with encoding=" + encoding.getCode());
				break;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.warn("could not decode image. encoding = {}, data length = {}, size = {} x {}: {}", encoding,
					data.length, width, height, e);
		}
		return image;
	}

}
