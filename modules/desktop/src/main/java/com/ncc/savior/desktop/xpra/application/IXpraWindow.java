package com.ncc.savior.desktop.xpra.application;

import java.io.Closeable;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowIconPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadataPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

public interface IXpraWindow extends Closeable {

	public void draw(DrawPacket packet);

	public void onWindowMoveResize(WindowMoveResizePacket packet);

	public void setDebugOutput(boolean debugOn);

	public void setWindowIcon(WindowIconPacket packet);

	public void updateWindowMetadata(WindowMetadataPacket packet);

	public void resize(int width, int height);
}
