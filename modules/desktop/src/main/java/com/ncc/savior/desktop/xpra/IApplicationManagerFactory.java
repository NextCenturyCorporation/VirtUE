package com.ncc.savior.desktop.xpra;

import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;

public interface IApplicationManagerFactory {

	XpraApplicationManager getApplicationManager(XpraClient client);

}
