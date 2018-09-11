package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Manages storing and retrieving images for Xen guest VMs.
 *
 */
public interface IXenGuestImageManager {

	void pushImageToStream(String path, OutputStream out);

	void storeStreamAsImage(String path, String type, InputStream uncloseableStream);

}
