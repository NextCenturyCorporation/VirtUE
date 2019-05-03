package com.ncc.savior.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class InputStreamConsumerTest {

	@Test
	void testRun() {
		StringBuilder stringBuilder = new StringBuilder();
		InputStream input = new ByteArrayInputStream("test input".getBytes());
		InputStreamConsumer isConsumer = new InputStreamConsumer(input, stringBuilder);
		isConsumer.run();
		assertNull(isConsumer.getException(), "exception was not null");
	}

	@Test
	void testGetException() {
		StringBuilder stringBuilder = new StringBuilder();
		InputStream input = new InputStream() {
			@Override
			public synchronized int read() throws IOException {
				throw new IOException("broken input stream");
			}
		};
		InputStreamConsumer isConsumer = new InputStreamConsumer(input, stringBuilder);
		isConsumer.run();
		assertNotNull(isConsumer.getException(), "exception was null");
	}

}
