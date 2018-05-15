package com.ncc.savior.desktop.clipboard;

import java.util.Collection;
import java.util.Set;

import com.ncc.savior.desktop.clipboard.data.ClipboardData;

public interface IClipboardWrapper {

	public void setDelayedRenderFormats(Collection<Integer> formats);

	public void setClipboardListener(IClipboardListener listener);

	public static interface IClipboardListener {
		/**
		 * Called after a delayed render response. Listener must put some data on the
		 * clipboard after receiving this call.
		 *
		 * @param format
		 */
		void onPasteAttempt(int format);

		void onClipboardChanged(Set<Integer> formats);
	}

	/**
	 * set clipboard data for a delayed render call.
	 * 
	 * @param clipboardData
	 */
	public void setDelayedRenderData(ClipboardData clipboardData);
}
