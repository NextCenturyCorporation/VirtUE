package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.ncc.savior.util.JavaUtil;

/**
 * Helper class to handle streaming commands over SSH using JSCH.
 * 
 *
 */
public class CommandHandler implements Closeable {
	private static Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private PrintStream ps;
	private BufferedReader br;
	private Channel channel;

	public CommandHandler(PrintStream ps, BufferedReader br, Channel myChannel) {
		this.ps = ps;
		this.br = br;
		this.channel = myChannel;
	}

	public String readLine() {
		try {
			String line = br.readLine();
			logger.debug(line);
			return line;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void sendln(String line) {
		ps.println(line);
	}

	public String readUtil(String trigger, String notString) {
		String line = null;
		while ((line = readLine()) != null) {
			if (line.contains(trigger) && !line.contains(notString)) {
				return line;
			}
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		JavaUtil.closeIgnoreErrors(ps, br);
		if (channel != null) {
			channel.disconnect();
		}

	}

}
