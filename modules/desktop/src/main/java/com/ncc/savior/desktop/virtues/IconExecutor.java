package com.ncc.savior.desktop.virtues;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IconExecutor {

	private ExecutorService executor;

	public IconExecutor() {
		executor = Executors.newFixedThreadPool(2);
	}

	public void submitThread(Runnable runnable) {
		executor.submit(runnable);
	}
}
