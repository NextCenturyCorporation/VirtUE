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
package com.ncc.savior.desktop.clipboard;

import java.io.Closeable;
import java.util.Set;

import com.ncc.savior.desktop.clipboard.data.ClipboardData;

/**
 * Generic interface to wrap clipboards from different Operating systems
 *
 *
 */
public interface IClipboardWrapper extends Closeable {

	/**
	 * Sets the clipboard in delayed rendering mode. This mode is named after
	 * Microsoft Windows name. In this mode, the first time a local application
	 * attempts to paste each format, a callback will be made. The callback is used
	 * to intercept and verify we want the past to occur based on policy.
	 *
	 * @param formats
	 */
	public void setDelayedRenderFormats(Set<ClipboardFormat> formats);

	/**
	 * sets the {@link IClipboardListener} to handle clipboard events.
	 *
	 * @param listener
	 */
	public void setClipboardListener(IClipboardListener listener);

	/**
	 * interface to handle clipboard events.
	 *
	 *
	 */
	public static interface IClipboardListener {
		/**
		 * Called after a delayed render response. Listener must put some data on the
		 * clipboard after receiving this call.
		 *
		 * @param format
		 */
		void onPasteAttempt(ClipboardFormat format);

		/**
		 * Called after a local application changes the clipboard.
		 *
		 * @param formats
		 */
		void onClipboardChanged(Set<ClipboardFormat> formats);

		/**
		 * Called when clipboard implementation has been closed
		 */
		void closed();
	}

	/**
	 * set clipboard data for a delayed render call. This will be called after a
	 * paste attempt has been signaled via
	 * {@link IClipboardListener#onPasteAttempt(int)}
	 *
	 * @param clipboardData
	 */
	public void setDelayedRenderData(ClipboardData clipboardData);

	/**
	 * Returns {@link ClipboardData} implementation from the current clipboard in
	 * the given format.
	 *
	 * @param format
	 * @return
	 */
	public ClipboardData getClipboardData(ClipboardFormat format);

}
