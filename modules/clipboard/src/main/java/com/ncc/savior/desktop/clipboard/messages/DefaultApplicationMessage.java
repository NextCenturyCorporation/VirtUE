package com.ncc.savior.desktop.clipboard.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to pass information from one virtue to another to run a default
 * application. The original driver for this is clicking on a url to open a
 * browser in another virtue.
 */
public class DefaultApplicationMessage extends BaseClipboardMessage {
	private static final long serialVersionUID = 1L;
	private List<String> arguments;
	private DefaultApplicationType defaultApplicationType;

	// For Jackson (de)serialization
	protected DefaultApplicationMessage() {
		this(null, null, (List<String>) null);
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType, String argument) {
		this(sourceId, defaultApplicationType, stringToList(argument));
	}

	private static List<String> stringToList(String argument) {
		List<String> list = new ArrayList<String>(1);
		list.add(argument);
		return list;
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType,
			List<String> arguments) {
		super(sourceId);
		this.defaultApplicationType = defaultApplicationType;
		this.arguments = arguments;
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType,
			String[] arguments) {
		this(sourceId, defaultApplicationType, arrayToList(arguments));
	}

	private static List<String> arrayToList(String[] args) {
		List<String> list = new ArrayList<>(args.length);
		for (String arg : args) {
			list.add(arg);
		}
		return list;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public DefaultApplicationType getDefaultApplicationType() {
		return defaultApplicationType;
	}

	@Override
	public String toString() {
		return "DefaultApplicationMessage [arguments=" + arguments + ", defaultApplicationType="
				+ defaultApplicationType + "]";
	}

	public enum DefaultApplicationType {
		BROWSER
	}
}
