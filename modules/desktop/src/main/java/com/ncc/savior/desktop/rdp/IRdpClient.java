package com.ncc.savior.desktop.rdp;

import java.io.IOException;

import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

public interface IRdpClient {

	Process startRdp(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color) throws IOException;

}
