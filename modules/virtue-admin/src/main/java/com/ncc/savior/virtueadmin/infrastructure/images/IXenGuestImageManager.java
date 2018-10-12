package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;

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
	 * runnable is called. Returns a runnable that normally should be passed to
	 * {@link IXenGuestImageManager#finishImageLoad(List)}. The caller can run the
	 * runnables, but implementations may have extra controls for running them
	 * efficiently. If Runnable is never called or passed back via
	 * {@link #finishImageLoad(List)}, file should be backed out.
	 * 
	 * @param path
	 *            - path is the image identifier used throughout the Savior system
	 *            for virtual machine images. It is not necessarily a file system
	 *            path and its actually meaning is determined by the
	 *            {@link ICloudManager} implementation.
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
