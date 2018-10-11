package com.ncc.savior.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Utility functions for import and export.
 * 
 *
 */
public class ImportExportUtils {
	private static final Logger logger = LoggerFactory.getLogger(ImportExportUtils.class);
	public static String VIRTUE_TEMPLATE_ZIP_ROOT = "virtues/";
	public static String APPLICATION_DEFN_ZIP_ROOT = "applications/";
	public static String VIRTUAL_MACHINE_TEMPLATE_ZIP_ROOT = "vms/";
	public static String VIRTUAL_MACHINE_IMAGE_ZIP_ROOT = "images/";
	public static String USER_ZIP_ROOT = "user/";
	public static String ICON_DEFN_ZIP_ROOT = "icons/";

	/**
	 * Read a zip file formatted for Savior import/export and store entries into the
	 * given collections. For image files, process with the given
	 * {@link BiConsumer}.
	 * 
	 * @param stream
	 * @param users
	 * @param vts
	 * @param vms
	 * @param apps
	 * @param icons
	 * @param vmImageConsumer
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static void readImportExportZipStream(InputStream stream, Collection<VirtueUser> users,
			Collection<VirtueTemplate> vts, Collection<VirtualMachineTemplate> vms,
			Collection<ApplicationDefinition> apps, Collection<IconModel> icons,
			BiConsumer<ZipEntry, InputStream> vmImageConsumer)
			throws JsonParseException, JsonMappingException, IOException {
		ZipEntry entry;
		try (ZipInputStream zipStream = new ZipInputStream(stream)) {
			while ((entry = zipStream.getNextEntry()) != null) {
				String name = entry.getName();
				InputStream uncloseableStream = new InputStream() {

					@Override
					public int read() throws IOException {
						return zipStream.read();
					}

					@Override
					public void close() {
						// do nothing so jsonMapper doesn't close the stream. We have to be careful to
						// close the other stream ourselves.
					}
				};
				ObjectMapper jsonMapper = new ObjectMapper();
				if (entry.isDirectory()) {
					// skip

				} else if (name.contains(ImportExportUtils.ICON_DEFN_ZIP_ROOT)) {
					byte[] buffer = new byte[1024];
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					int len;
					while ((len = IOUtils.read(uncloseableStream, buffer)) > 0) {
						os.write(buffer, 0, len);
					}
					os.flush();
					String id = name.substring(ImportExportUtils.ICON_DEFN_ZIP_ROOT.length(), name.length() - 4);
					byte[] data = os.toByteArray();
					IconModel icon = new IconModel(id, data);
					icons.add(icon);
				} else if (name.contains(ImportExportUtils.APPLICATION_DEFN_ZIP_ROOT)) {
					ApplicationDefinition app = jsonMapper.readValue(uncloseableStream, ApplicationDefinition.class);
					apps.add(app);
				} else if (name.contains(ImportExportUtils.VIRTUAL_MACHINE_TEMPLATE_ZIP_ROOT)) {
					VirtualMachineTemplate vm = jsonMapper.readValue(uncloseableStream, VirtualMachineTemplate.class);
					vms.add(vm);
				} else if (name.contains(ImportExportUtils.VIRTUE_TEMPLATE_ZIP_ROOT)) {
					VirtueTemplate virtue = jsonMapper.readValue(uncloseableStream, VirtueTemplate.class);
					vts.add(virtue);
				} else if (name.contains(ImportExportUtils.USER_ZIP_ROOT)) {
					VirtueUser user = jsonMapper.readValue(uncloseableStream, VirtueUser.class);
					users.add(user);
				} else if (name.contains(ImportExportUtils.VIRTUAL_MACHINE_IMAGE_ZIP_ROOT)) {
					vmImageConsumer.accept(entry, uncloseableStream);
				}
				// logger.debug("Entry: " + entry.getName() + " " + entry.isDirectory() + " " +
				// entry.getSize());
			}
		}
	}
}
