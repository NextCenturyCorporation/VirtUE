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
	private List<Object> arguments;
	private DefaultApplicationType defaultApplicationType;

	// For Jackson (de)serialization
	protected DefaultApplicationMessage() {
		this(null, null, null);
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType, Object argument) {
		this(sourceId, defaultApplicationType, objectToList(argument));
	}

	private static List<Object> objectToList(Object argument) {
		List<Object> list = new ArrayList<Object>(1);
		list.add(argument);
		return list;
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType,
			List<Object> arguments) {
		super(sourceId);
		this.defaultApplicationType = defaultApplicationType;
		this.arguments = arguments;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public DefaultApplicationType getDefaultApplicationType() {
		return defaultApplicationType;
	}

	public enum DefaultApplicationType {
		BROWSER
	}
}
