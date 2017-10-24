package com.ncc.savior.desktop.xpra.application;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

public interface IXpraWindow {

	public void close();

	public void draw(DrawPacket packet);

	public void onWindowMoveResize(WindowMoveResizePacket packet);

	public void setDebugOutput(boolean debugOn);
}
