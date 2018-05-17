package com.ncc.savior.desktop.clipboard.hub;

import com.ncc.savior.desktop.clipboard.connection.IClipboardConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;

public interface IConnectionFactory {

	IClipboardConnection createConnection(IConnectionParameters connectionParams);

}
