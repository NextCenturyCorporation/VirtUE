package com.ncc.savior.desktop.rdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

public class FreeRdpClient implements IRdpClient {
	private static final Logger logger = LoggerFactory.getLogger(FreeRdpClient.class);
	private File exe;

	public FreeRdpClient(File freerdpExe) {
		this.exe = freerdpExe;
	}

	@Override
	public Process startRdp(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color) throws IOException {
		String user = app.getUserName();
		String host = app.getHostname();
		String appPath = app.getWindowsApplicationPath();
		String password = app.getPrivateKey();
		String params = String.format("/f /u:%s /v:%s /app:\"%s\" /p:\"%s\"", user, host, appPath, password);
		String[] params2 = new String[] { exe.getAbsolutePath(), "/f", "/u:" + user, "/v:" + host,
				"/app:\"" + appPath + "\"", "/p:\"" + password + "\"" };
		logger.debug("Exe=" + exe.getAbsolutePath() + " Params=" + params);

		// Process p = Runtime.getRuntime().exec(exe.getAbsolutePath() + " " + params);
		Process p = new ProcessBuilder(params2)
				.redirectErrorStream(true)
				.redirectOutput(Redirect.INHERIT)
				// .redirectOutput(new File("logs/rdp.out"))
				.start();
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
			}
		});
		t.start();
		sleep(1000);
		return p;
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		String version = "1";
		String hostname = args[1];
		DesktopVirtueApplication dva = new DesktopVirtueApplication("", "c:\\windows\\notepad.exe", version, OS.WINDOWS,
				hostname, 3389, "Administrator", args[0], "c:\\windows\\notepad.exe");
		DesktopVirtue virtue = new DesktopVirtue(UUID.randomUUID().toString(), "name", "tempId");

		Process p = new FreeRdpClient(new File("c:\\wfreerdp.exe")).startRdp(dva, virtue, null);

		if (p != null && p.isAlive()) {
			log(p.getInputStream());
			log(p.getErrorStream());
		}
		p.waitFor();
	}

	private static void sleep(long sleepMillis) {
		try {
			Thread.sleep(sleepMillis);
		} catch (InterruptedException e) {
			// do nothing
		}
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
