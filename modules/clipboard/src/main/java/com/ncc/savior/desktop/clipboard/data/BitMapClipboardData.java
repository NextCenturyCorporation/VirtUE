package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.windows.NativelyDeallocatedMemory;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

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

	@Override
	public Pointer createWindowsData() {
		try {
			long length = getWindowsDataLengthBytes();
			Memory winMemory = new NativelyDeallocatedMemory(length);
			winMemory.clear();
			winMemory.write(0, bitMapData, 0, bitMapData.length);
			return winMemory;
		} catch (Throwable t) {
			logger.error("remove me error ", t);
			throw t;
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
