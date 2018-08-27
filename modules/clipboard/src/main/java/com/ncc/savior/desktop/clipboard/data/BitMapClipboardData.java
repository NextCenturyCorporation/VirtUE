package com.ncc.savior.desktop.clipboard.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;

/**
 * Clipboard Data container used for copying files between virtues. This class
 * transports the files as a byte array and therefore shouldn't be used for
 * large data.
 * 
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

	// private BitMapClipboardData convertBitmapPngData(Pointer p) {
	// HBITMAP hbitmap = new HBITMAP(p);
	//
	// BITMAP gdiBitMap = new BITMAP();
	// gdi32.GetObject(hbitmap, gdiBitMap.size(), gdiBitMap.getPointer());
	// gdiBitMap.autoRead();
	// BITMAPINFO info = new BITMAPINFO();
	// info.bmiHeader.biSize = info.size();
	// info.bmiHeader.biWidth = gdiBitMap.bmWidth.intValue();
	// info.bmiHeader.biHeight = gdiBitMap.bmHeight.intValue();
	// info.bmiHeader.biPlanes = 1;
	// info.bmiHeader.biBitCount = 32;
	// info.bmiHeader.biCompression = WinGDI.BI_RGB;
	// info.autoWrite();
	// HDC hDC = user32.GetDC(windowHandle);
	// int sizeBytes = info.bmiHeader.biWidth * info.bmiHeader.biHeight * 4;
	// Pointer lpvBits = new Memory(sizeBytes);
	// int status = gdi32.GetDIBits(hDC, hbitmap, 0, gdiBitMap.bmHeight.intValue(),
	// lpvBits, info,
	// WinGDI.DIB_RGB_COLORS);
	// BufferedImage image = new BufferedImage(info.bmiHeader.biWidth,
	// info.bmiHeader.biHeight,
	// BufferedImage.TYPE_INT_ARGB);
	// int i = 0;
	// for (int y = image.getHeight() - 1; y >= 0; y--) {
	// for (int x = 0; x < image.getWidth(); x++) {
	// int rgb = 0;
	// int r = lpvBits.getByte(i + 2);
	// int g = lpvBits.getByte(i + 1);
	// int b = lpvBits.getByte(i);
	// int a = lpvBits.getByte(i + 3);
	// rgb = (a << 24) + (r << 16) + (g << 8) + (b << 0);
	// image.setRGB(x, y, rgb);
	// i += 4;
	// }
	// }
	// ByteArrayOutputStream output = new ByteArrayOutputStream();
	// try {
	// ImageIO.write(image, "PNG", output);
	// } catch (IOException e) {
	// logger.debug("error writing image into PNG format!", e);
	// }
	// return new BitMapClipboardData(output.toByteArray());
	// }

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

			ImageIO.write(image, "png", new File("toWindows.png"));
			Pointer lpvBits = new Memory(numBytesForBuffer);
			for (int y = image.getHeight() - 1; y >= 0; y--) {
				for (int x = 0; x < image.getWidth(); x++) {
					int rgb = image.getRGB(x, y);
					int r = (rgb) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb >> 16) & 0xFF;
					int a = (rgb >> 24) & 0xFF;
					// logger.debug("RGH hex=" + Integer.toHexString(rgb) + " r=" +
					// Integer.toHexString(r) + " g="
					// + Integer.toHexString(g) + " b=" + Integer.toHexString(b) + " a="
					// + Integer.toHexString(a));
					a |= 0xFF;
					lpvBits.setByte(i, (byte) r);
					lpvBits.setByte(i + 1, (byte) g);
					lpvBits.setByte(i + 2, (byte) b);
					lpvBits.setByte(i + 3, (byte) a);
					i += bytesPerPixel;
				}
			}
			logger.debug("buffer=" + lpvBits.dump(0, 32));
			int scanLinesWritten = wrapper.getGdi32().SetDIBits(hDC, hbitmap, 0, height, lpvBits, lpbi,
					WinGDI.DIB_RGB_COLORS);

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
		return bitMapData.length;
	}

	@Override
	public String toString() {
		return "BitMapClipboardData [bitMapData size=" + bitMapData.length + "]";
	}

}
