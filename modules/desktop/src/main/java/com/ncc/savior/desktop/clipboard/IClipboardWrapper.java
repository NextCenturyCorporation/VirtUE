package com.ncc.savior.desktop.clipboard;

import java.util.Collection;
import java.util.Set;

import com.ncc.savior.desktop.clipboard.data.ClipboardData;

/**
 * Generic interface to wrap clipboards from different Operating systems
 *
 *
 */
public interface IClipboardWrapper {

	/**
	 * Sets the clipboard in delayed rendering mode. This mode is named after
	 * Microsoft Windows name. In this mode, the first time a local application
	 * attempts to paste each format, a callback will be made. The callback is used
	 * to intercept and verify we want the past to occur based on policy.
	 *
	 * @param formats
	 */
	public void setDelayedRenderFormats(Collection<ClipboardFormat> formats);

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
