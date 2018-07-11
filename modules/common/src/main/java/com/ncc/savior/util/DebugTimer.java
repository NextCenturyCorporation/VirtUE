package com.ncc.savior.util;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugTimer {
	private static final Logger logger = LoggerFactory.getLogger(DebugTimer.class);
	private String name;
	private long startMillis;
	private long lastInterval;
	private ArrayList<String> intervalResults;

	public DebugTimer(String name) {
		this.intervalResults = new ArrayList<String>(15);
		this.name = name;
		this.startMillis = System.currentTimeMillis();
		this.lastInterval = startMillis;
	}

	public void stop() {
		long now = System.currentTimeMillis();
		interval("end");
		long elapsed = now - startMillis;
		logger.debug("Timer " + name + " : " + getPrettyTime(elapsed));
		if (intervalResults.size() > 1) {
			for (String result : intervalResults) {
				logger.debug("  " + result);
			}
		}
	}

	private String getPrettyTime(long elapsed) {
		return elapsed / 1000.0 + "s";
	}

	public void interval(String string) {
		long now = System.currentTimeMillis();
		long elapsed = now - lastInterval;
		lastInterval = now;
		intervalResults.add(string + " " + getPrettyTime(elapsed));

	}
}
