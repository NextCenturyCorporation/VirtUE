package com.ncc.savior.desktop.clipboard.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.windows.NativelyDeallocatedMemory;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class FileClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FileClipboardData.class);
	private List<File> files;
	private byte[] zipData;

	public FileClipboardData(List<File> files) {
		super(ClipboardFormat.FILES);
		this.files = files;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zs = new ZipOutputStream(baos)) {
			for (File file : files) {
				ZipEntry ze = new ZipEntry(file.getName());
				try {
					zs.putNextEntry(ze);
					Files.copy(file.toPath(), zs);
					zs.closeEntry();
				} catch (IOException e) {
					logger.error("Error attempting to zip file " + file.getName() + " for transmission", e);
				}
			}
		} catch (IOException e) {
			logger.error("zip failed", e);
		}
		this.zipData = baos.toByteArray();
		logger.debug("BAOS (" + baos.size() + "): " + Arrays.toString(zipData));
	}

	public static void pack(String sourceDirPath) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zs = new ZipOutputStream(baos)) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp).filter(path -> !Files.isDirectory(path)).forEach(path -> {
				ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
				try {
					zs.putNextEntry(zipEntry);
					Files.copy(path, zs);
					zs.closeEntry();
				} catch (IOException e) {
					logger.error("Error attempting to zip file for transmission");
				}
			});
		}
	}

	@Override
	public Pointer createWindowsData() {
		writeFilesFromZip();
		Memory winMemory = new NativelyDeallocatedMemory(getWindowsDataLengthBytes());
		winMemory.clear();
		String data = getClipboardStringData();
		winMemory.setString(0, data);
		return winMemory;
	}

	@Override
	public Pointer createLinuxData() {
		writeFilesFromZip();
		String path = getClipboardStringData();
		int size = 1 * (path.length());
		Memory mem = new Memory(size + 1);
		mem.clear();
		mem.setString(0, path);
		return mem;
	}

	private void writeFilesFromZip() {
		ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
		try (ZipInputStream zs = new ZipInputStream(bais)) {
			ZipEntry entry;
			int size;
			byte[] buffer = new byte[2048];
			while ((entry = zs.getNextEntry()) != null) {
				String name = entry.getName();
				File file = File.createTempFile("", name);
				logger.debug("writing file " + name + " to " + file.getAbsolutePath());
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
				while ((size = zs.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}
				bos.flush();
				bos.close();
				logger.debug("finished writting " + file.getAbsolutePath());
			}
		} catch (IOException e) {
			logger.error("zip failed", e);
		}
	}

	@Override
	public int getLinuxNumEntries() {
		String path = getClipboardStringData();
		return path.length();
	}

	@Override
	public int getLinuxEntrySizeBits() {
		return 8;
	}

	@Override
	public long getWindowsDataLengthBytes() {
		String data = getClipboardStringData();
		return 1 * (data.getBytes().length + 1);
	}

	private String getClipboardStringData() {
		StringBuilder sb = new StringBuilder();
		sb.append("copy");
		for (File file : files) {
			sb.append(System.lineSeparator()).append(file);
		}
		return files.get(0).getAbsolutePath();
	}

}
