package com.ncc.savior.desktop.rdp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

/**
 * Utility class to handle starting the local RDP client instead of our own
 * client.
 *
 */

public class WindowsRdp implements IRdpClient {

	private static final Logger logger = LoggerFactory.getLogger(WindowsRdp.class);

	@Override
	public Process startRdp(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color) throws IOException {
		return startRdp(app, virtue, color, false);
	}

	protected static Process startRdp(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color,
			boolean logRdpFile) throws IOException {

		File tempFile = File.createTempFile(virtue.getId(), ".rdp");
		String nl = System.getProperty("line.separator");
		BufferedWriter writer = null;
		Process p = null;
		try {
			deletePassword(app.getHostname());
			sleep(500);
			savePassword(app.getHostname(), app.getUserName(), app.getPrivateKey());
			sleep(500);
			writer = new BufferedWriter(new FileWriter(tempFile));
			writer.write("full address:s:" + app.getHostname());
			writer.write(nl);
			writer.write("alternate shell:s:" + app.getWindowsApplicationPath());
			writer.write(nl);
			writer.write("remoteapplicationmode:i:0");
			writer.write(nl);
			writer.write("prompt for credentials:i:0");
			writer.write(nl);
			writer.close();
			writer = null;
			if (logRdpFile) {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(tempFile));
					String line = null;
					logger.debug("RDP File:");
					while ((line = reader.readLine()) != null) {
						logger.debug(line);
					}
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
			}

			p = Runtime.getRuntime().exec("mstsc.exe " + tempFile.getAbsolutePath());
			final Process process = p;
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						process.waitFor();
						logger.info("RDP was closed");
					} catch (InterruptedException e) {
						process.destroy();
						logger.info("RDP was destroyed");
					}
					try {
						deletePassword(app.getHostname());
					} catch (IOException e) {
						// do nothing
					}
				}
			});
			t.start();
			sleep(1000);
		} finally {

			if (writer != null) {
				writer.close();
			}
			tempFile.delete();
		}
		return p;
	}

	private static void sleep(long sleepMillis) {
		try {
			Thread.sleep(sleepMillis);
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	private static void savePassword(String hostname, String user, String password) throws IOException {
		String command = "cmdkey /generic:\"" + hostname + "\" /user:\"" + user + "\" /pass:\"" + password + "\"";
		Process p = Runtime.getRuntime().exec(command);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	private static void deletePassword(String hostname) throws IOException {
		String command = "cmdkey /delete:\"" + hostname + "\"";
		Process p = Runtime.getRuntime().exec(command);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		String version = "1";
		String hostname = args[1];
		DesktopVirtueApplication dva = new DesktopVirtueApplication("", "calc.exe", version, OS.WINDOWS, hostname, 3389,
				"Administrator", args[0], "calc.exe");
		DesktopVirtue virtue = new DesktopVirtue(UUID.randomUUID().toString(), "name", "tempId");
		Process p = new WindowsRdp().startRdp(dva, virtue, null);

		if (p != null && p.isAlive()) {
			log(p.getInputStream());
			log(p.getErrorStream());
		}
		p.waitFor();
	}

	private static void log(InputStream stream) throws IOException {
		Thread t = new Thread();
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		t.setDaemon(true);
		t.start();

	}
}
