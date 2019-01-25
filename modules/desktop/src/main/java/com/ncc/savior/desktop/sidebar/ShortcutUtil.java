package com.ncc.savior.desktop.sidebar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShortcutUtil {

	public static void createShortcut(String virtueName, String appName, String virtueId, String applicationId) {
		String path = "C:\\VirtUE\\modules\\desktop\\desktop-0.1.0-SNAPSHOT-all.jar";
		String args = virtueId + " " + applicationId;
		path = '"' + path + '"';
		try {
			File file = File.createTempFile("shortcut_geni", ".vbs");
			file.deleteOnExit();
			try (FileWriter fw = new java.io.FileWriter(file)) {
				String vbs = "Set oWS = WScript.CreateObject(\"WScript.Shell\")  \n"
						+ "sLinkFile = oWS.ExpandEnvironmentStrings(\"%HOMEDRIVE%%HOMEPATH%\\Desktop\\" + appName + " "
						+ virtueName + ".lnk\")\n"
						+ "Set oLink = oWS.CreateShortcut(sLinkFile)\n "
						+ "oLink.TargetPath = oWS.ExpandEnvironmentStrings(" + path + ")\n" + "oLink.Arguments = \""
						+ args + "\"\n"
						+ "oLink.IconLocation = \"C:\\VirtUE\\modules\\desktop\\src\\main\\resources\\images\\saviorLogo.ico\"\n"
						+ "oLink.WorkingDirectory = \"C:\\VirtUE\\modules\\desktop\"\n"
						+ "oLink.Save \n";
				fw.write(vbs);
			}
			Process p = Runtime.getRuntime().exec("wscript " + file.getPath());
			p.waitFor();

		} catch (IOException | InterruptedException e) {
			System.out.println("" + e);
		}

	}

}
