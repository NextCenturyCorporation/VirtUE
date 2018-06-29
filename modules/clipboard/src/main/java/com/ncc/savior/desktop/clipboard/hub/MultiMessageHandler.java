package com.ncc.savior.desktop.clipboard.hub;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public class MultiMessageHandler implements IClipboardMessageHandler {

	private Set<IClipboardMessageHandler> handlers;

	public MultiMessageHandler() {
		handlers = new LinkedHashSet<IClipboardMessageHandler>();
	}

	public void addHandler(IClipboardMessageHandler handler) {
		handlers.add(handler);
	}

	public void removeHandler(IClipboardMessageHandler handler) {
		handlers.remove(handler);
	}

	@Override
	public void onMessage(IClipboardMessage message, String groupId) {
		handlers.parallelStream().forEach((handler) -> {
			handler.onMessage(message, groupId);
		});

	}

	@Override
	public void onMessageError(String description, IOException e) {
		handlers.parallelStream().forEach((handler) -> {
			handler.onMessageError(description, e);
		});

	}

	@Override
	public void closed() {
		handlers.parallelStream().forEach((handler) -> {
			handler.closed();
		});

	}

}
