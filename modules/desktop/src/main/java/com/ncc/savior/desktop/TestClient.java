package com.ncc.savior.desktop;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.debug.DebugPacketHandler;

public class TestClient {
    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) {
        logger.debug("started");
        logger.info("info");
        logger.trace("trace");
		XpraClient client = new XpraClient(null);
		File dir = DebugPacketHandler.getDefaultTimeBasedDirectory();
		DebugPacketHandler debugHandler = new DebugPacketHandler(dir);
		client.addPacketListener(debugHandler);
		client.addPacketSendListener(debugHandler);
        client.connect(new TcpConnectionFactory(), new TcpConnectionFactory.TcpConnectionParameters("localhost", 10000));

    }
}
