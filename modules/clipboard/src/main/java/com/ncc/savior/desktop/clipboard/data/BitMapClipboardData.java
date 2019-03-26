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
package com.ncc.savior.desktop.clipboard.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;

/**
 * Clipboard Data container used for copying images between virtues. This class
 * takes a byte array of an image in PNG format. The image must be converted
 * prior to this class.
 * 
 * Windows Quirks:
 * <ol>
 * <li>PNG Option - Although BITMAPINFOHEADER has an option for compression as
 * BI_PNG, that isn't really a valid format for the clipboard or windows itself.
 * It is intended for sending images to printers which have hardware support for
 * png. Therefore, windows returns a RGB bitmap which java converts to a PNG for
 * transport.
 * <li>Transparency - Windows clipboard bitmap does not support transparency.
 * 
 */
public class BitMapClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(BitMapClipboardData.class);
	private byte[] bitMapData;

	public BitMapClipboardData(byte[] data) {
		super(ClipboardFormat.BITMAP);
		this.bitMapData = data;
	}

	@Override
	public Pointer createWindowsData(WindowsClipboardWrapper wrapper) {
		// read bitmap data as png into buffered image
		// use image.getRGB to get integer and store into byte array in BGRA format
		// use CreateCompatibleBitmap to create HBITMAP
		// pass byte array into gdi32.setDIBits() to write to new HBITMAP
		// create HBITMAP??
		// this windows API is rather ridiculous

		try {
			int bytesPerPixel = WindowsClipboardWrapper.BITS_PER_PIXEL / 8;
			HDC hDC = wrapper.getUser32().GetDC(wrapper.getWindowHandle());
			ByteArrayInputStream input = new ByteArrayInputStream(bitMapData);
			BufferedImage image = ImageIO.read(input);
			int width = image.getWidth();
			int height = image.getHeight();

			HBITMAP hbitmap = wrapper.getGdi32().CreateCompatibleBitmap(hDC, width, height);
			BITMAPINFO lpbi = WindowsClipboardWrapper.getBitmapInfo(width, height,
					WindowsClipboardWrapper.BITS_PER_PIXEL);
			int numBytesForBuffer = width * height * bytesPerPixel;
			int i = 0;

			Pointer lpvBits = new Memory(numBytesForBuffer);
			for (int y = image.getHeight() - 1; y >= 0; y--) {
				for (int x = 0; x < image.getWidth(); x++) {
					int rgb = image.getRGB(x, y);
					int r = (rgb) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb >> 16) & 0xFF;
					// windows doesn't store transparency on bitmap clipboard
					// int a = (rgb >> 24) & 0xFF;
					// logger.debug("RGH hex=" + Integer.toHexString(rgb) + " r=" +
					// Integer.toHexString(r) + " g="
					// + Integer.toHexString(g) + " b=" + Integer.toHexString(b) + " a="
					// + Integer.toHexString(a));
					int a = 0xFF;
					lpvBits.setByte(i, (byte) r);
					lpvBits.setByte(i + 1, (byte) g);
					lpvBits.setByte(i + 2, (byte) b);
					lpvBits.setByte(i + 3, (byte) a);
					i += bytesPerPixel;
				}
			}
			int scanLinesWritten = wrapper.getGdi32().SetDIBits(hDC, hbitmap, 0, height, lpvBits, lpbi,
					WinGDI.DIB_RGB_COLORS);
			if (scanLinesWritten != height) {
				logger.warn(
						"Warning! Windows bitmap conversion did not write the expected amount of lines.  linesWritten="
								+ scanLinesWritten + " ExpectedImageHeight=" + height);
			}

			return hbitmap.getPointer();
		} catch (IOException e) {
			String msg = "Error converting bitmap clipboard data to windows data.";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public Pointer createLinuxData() {
		int length = bitMapData.length;
		int size = 1 * (length);
		Memory mem = new Memory(size + 1);
		mem.clear();
		mem.write(0, bitMapData, 0, length);
		return mem;
	}

	@Override
	public int getLinuxNumEntries() {
		return bitMapData.length;
	}

	@Override
	public int getLinuxEntrySizeBits() {
		return 8;
	}

	@Override
	public long getWindowsDataLengthBytes() {
		throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR,
				"Windows data length for " + this.getClass().getSimpleName()
						+ " is determined when data is created.  This function shouldn't ever be called.");
	}

	@Override
	public String toString() {
		return "BitMapClipboardData [bitMapData size=" + bitMapData.length + "]";
	}

}
