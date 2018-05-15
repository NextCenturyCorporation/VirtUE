package com.ncc.savior.desktop.clipboard;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public interface IClipboardManager {

	void attachClient(DesktopVirtue virtue, XpraClient client);

}
