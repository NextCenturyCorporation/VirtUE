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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

/**
 * Clipboard Data container used for copying files between virtues. This class
 * transports the files as a byte array and therefore shouldn't be used for
 * large data.
 * 
 *
 */
public class FileClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FileClipboardData.class);
	private static final int WIN_DROPFILES_BASE_BYTES = 20;
	private boolean windowsWideText = true;
	private List<File> sourceFiles;
	private List<File> destinationFiles;
	private byte[] zipData;
	private static File TEMP_DIR = new File(".tmp");
	private static String dateFormat = "yyyyMMdd-HHmmssSSS";
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

	public FileClipboardData(List<File> files) {
		super(ClipboardFormat.FILES);
		this.sourceFiles = files;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zs = new ZipOutputStream(baos)) {
			for (File file : files) {
				if (file.isDirectory()) {
					logger.debug("walking directory: " + file.getAbsolutePath());
					Path pp = file.getParentFile().toPath();
					Files.walk(file.toPath()).filter(path -> !Files.isDirectory(path)).forEach(path -> {
						String relPath = pp.relativize(path).toString();
						logger.debug("relative path=" + relPath);
						ZipEntry zipEntry = new ZipEntry(relPath);
						try {
							zs.putNextEntry(zipEntry);
							Files.copy(path, zs);
							zs.closeEntry();
						} catch (IOException e) {
							logger.error("Error attempting to zip file for transmission");
						}
					});
				}
				if (file.isFile()) {
					ZipEntry ze = new ZipEntry(file.getName());
					try {
						zs.putNextEntry(ze);
						Files.copy(file.toPath(), zs);
						zs.closeEntry();
					} catch (IOException e) {
						logger.error("Error attempting to zip file " + file.getName() + " for transmission", e);
					}
				}
			}
		} catch (IOException e) {
			logger.error("zip failed", e);
		}
		this.zipData = baos.toByteArray();
		this.windowsWideText = true;
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

	/**
	 * Windows documentation mentions a STGMEDIUM structure, that inspection didn't
	 * seem to be used. Instead, we write a DROPFILES structure.
	 * 
	 * Copy/paste files -
	 * https://docs.microsoft.com/en-us/windows/desktop/shell/clipboard#formats-for-transferring-file-system-objects
	 * DROPFILES -
	 * https://docs.microsoft.com/en-us/windows/desktop/api/shlobj_core/ns-shlobj_core-_dropfiles
	 */
	@Override
	public Pointer createWindowsData() {
		try {
			boolean wide = windowsWideText;
			destinationFiles = writeFilesFromZip();
			long length = getWindowsDataLengthBytes();
			Memory winMemory = new NativelyDeallocatedMemory(length);
			winMemory.clear();
			// offset is always 20, 4 bytes
			winMemory.setInt(0, 20);
			// 8 bytes of 0 for point member
			// 4 bytes of boolean false
			// 4 bytes of boolean true for wide text.
			winMemory.setInt(16, 1);
			// rest for string, double null terminated
			int i = 20;
			for (File file : destinationFiles) {
				if (file.exists()) {
					String path = file.getAbsolutePath();
					if (wide) {
						winMemory.setWideString(i, path);
						i += (path.length() + 1) * 2;
					} else {
						winMemory.setString(i, path);
						i += (path.length() + 1) * 2;
					}
				}
				winMemory.setByte(i, (byte) 0);
			}
			return winMemory;
		} catch (Throwable t) {
			logger.error("remove me error ", t);
			throw t;
		}
	}

	@Override
	public Pointer createLinuxData() {
		destinationFiles = writeFilesFromZip();
		String path = getClipboardStringData(destinationFiles);
		int size = 1 * (path.length());
		Memory mem = new Memory(size + 1);
		mem.clear();
		mem.setString(0, path);
		return mem;
	}

	private List<File> writeFilesFromZip() {
		if (!TEMP_DIR.exists()) {
			TEMP_DIR.mkdirs();
			TEMP_DIR.deleteOnExit();
		}
		File xferDir = getThisTransferDir();
		List<File> newFiles = new ArrayList<File>();
		ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
		try (ZipInputStream zs = new ZipInputStream(bais)) {
			ZipEntry entry;
			int size;
			byte[] buffer = new byte[2048];
			while ((entry = zs.getNextEntry()) != null) {
				String name = entry.getName();
				File file = new File(xferDir, name);
				if (logger.isTraceEnabled()) {
					logger.trace("writing file " + name + " to " + file.getAbsolutePath());
				}
				file.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
				while ((size = zs.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}
				bos.flush();
				bos.close();
				file.deleteOnExit();
				if (logger.isTraceEnabled()) {
					logger.trace("finished writting " + file.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			logger.error("zip failed", e);
		}
		File[] files = xferDir.listFiles();
		for (File file : files) {
			newFiles.add(file);
		}
		return newFiles;
	}

	private static File getThisTransferDir() {
		String date = dateFormatter.format(new Date());
		File file = new File(TEMP_DIR, date);
		file.mkdirs();
		file.deleteOnExit();
		return file;
	}

	@Override
	public int getLinuxNumEntries() {
		String path = getClipboardStringData(destinationFiles);
		return path.length();
	}

	@Override
	public int getLinuxEntrySizeBits() {
		return 8;
	}

	@Override
	public long getWindowsDataLengthBytes() {
		// Documentation says the CF_HDROP format is STGMEDIUM with the union being
		// HGLOBAL which is actually a DROPFILES structure. Inspection seems to indicate
		// it is only a DROPFILES structure.
		boolean wide = windowsWideText;
		int size = WIN_DROPFILES_BASE_BYTES;
		for (File file : destinationFiles) {
			if (file.exists()) {
				int lengthPlusNull = file.getAbsolutePath().length() + 1;
				lengthPlusNull = (wide ? lengthPlusNull * 2 : lengthPlusNull);
				size += lengthPlusNull;
			}
			// add for 2nd of ending double null.
			size += (wide ? 2 : 1);
		}
		return size;
	}

	private String getClipboardStringData(List<File> files) {
		StringBuilder sb = new StringBuilder();
		sb.append("copy");
		for (File file : files) {
			sb.append(System.lineSeparator()).append("file://").append(file.getAbsolutePath());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "FileClipboardData [sourceFiles=" + sourceFiles + ", destinationFiles=" + destinationFiles + "]";
	}

}
