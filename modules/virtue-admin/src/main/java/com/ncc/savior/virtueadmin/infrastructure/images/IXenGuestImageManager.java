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
package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;

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
	 * efficiently. If Runnable is never called or passed to
	 * {@link #finishImageLoad(List)}, file should not be saved.
	 * 
	 * @param path
	 *            - path is the image identifier used throughout the Savior system
	 *            for virtual machine images. It is not necessarily a file system
	 *            path and its actually meaning is determined by the
	 *            {@link ICloudManager} implementation.  See {@link VirtualMachineTemplate}
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
