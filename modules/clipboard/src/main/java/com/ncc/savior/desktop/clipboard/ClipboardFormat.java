package com.ncc.savior.desktop.clipboard;

import java.util.HashMap;
import java.util.Map;

public enum ClipboardFormat {
	TEXT(1, "STRING"), UNICODE(13, "UTF8_STRING"), HTML(13, "TEXT/HTML");

	private static Map<Integer, ClipboardFormat> windowsToLinux;
	private static Map<String, ClipboardFormat> linuxToWindows;

	static {
		windowsToLinux = new HashMap<Integer, ClipboardFormat>();
		linuxToWindows = new HashMap<String, ClipboardFormat>();
		for (ClipboardFormat v : ClipboardFormat.values()) {
			windowsToLinux.put(v.windows, v);
			linuxToWindows.put(v.linux, v);
		}
	}

	private String linux;
	private int windows;

	ClipboardFormat(int windows, String linux) {
		this.windows = windows;
		this.linux = linux;
	}

	public int getWindows() {
		return windows;
	}

	public String getLinux() {
		return linux;
	}

	public static ClipboardFormat fromWindows(int intValue) {
		return windowsToLinux.get(intValue);
	}

	public static ClipboardFormat fromLinux(String str) {
		return linuxToWindows.get(str);
	}
}
