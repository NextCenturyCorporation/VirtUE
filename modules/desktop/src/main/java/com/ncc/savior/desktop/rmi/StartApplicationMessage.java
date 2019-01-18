package com.ncc.savior.desktop.rmi;

import java.util.Arrays;
import java.util.List;

public class StartApplicationMessage {
	private List<String> arguments;

	// For Jackson (de)serialization
	protected StartApplicationMessage() {
		this(null);
	}

	public StartApplicationMessage(String[] arguments) {
		this.arguments = Arrays.asList(arguments);
	}

}
