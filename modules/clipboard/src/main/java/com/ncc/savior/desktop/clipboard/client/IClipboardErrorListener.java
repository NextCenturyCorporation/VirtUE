package com.ncc.savior.desktop.clipboard.client;

/**
 * Indicates that an error has occurred and the clipboard client is no longer
 * working.
 * 
 *
 */
public interface IClipboardErrorListener {

	/**
	 * Error description and/or exception included. Neither are required but often
	 * provided for more information.
	 * 
	 * @param description
	 * @param e
	 */
	void onError(String description, Exception e);

}
