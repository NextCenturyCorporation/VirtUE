package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages storing and retrieving images for Xen guest VMs.
 *
 */
public interface IXenGuestImageManager {

	/**
	 * Writes a given image to the output stream.
	 * 
	 * @param path
	 * @param out
	 * @throws IOException
	 */
	void pushImageToStream(String path, OutputStream out) throws IOException;

	/**
	 * Needed special provisions for our Windows VMs. Writes the given image to the
	 * output stream for a windows image.
	 * 
	 * @param path
	 * @param out
	 * @throws IOException
	 */
	void pushImageToStreamWindows(String path, OutputStream out) throws IOException;

	/**
	 * Prepare the image upload. Should not actually complete an upload until
	 * runnable is called. If Runnable is never called, file should be backed out.
	 * 
	 * @param path
	 * @param type
	 * @param uncloseableStream
	 * @throws IOException
	 */
	Runnable prepareImageUpload(String path, String type, InputStream uncloseableStream) throws IOException;

	/**
	 * Initiate push of image to final destination. Once called, the images cannot
	 * be canceled. The returned future indicates when all loads have completed or
	 * one failed.
	 * 
	 * @param imageCompletionRunnables
	 * @return
	 */
	CompletableFuture<Void> finishImageLoad(List<Runnable> imageCompletionRunnables);

}
