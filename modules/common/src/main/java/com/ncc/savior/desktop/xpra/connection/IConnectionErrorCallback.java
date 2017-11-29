package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

/**
 * Simple callback for errors on connections so we can make sure it gets routed
 * to the appropriate place.
 *
 *
 */
public interface IConnectionErrorCallback {
	public void onError(String description, IOException e);
}
