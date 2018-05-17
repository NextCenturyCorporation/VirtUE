package com.ncc.savior.desktop.clipboard.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.messages.BaseClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.util.JavaUtil;

public class JsonObjectMessageSerializer implements IMessageSerializer {
	private ObjectMapper jsonMapper;
	private IConnectionWrapper connection;
	private InputStream in;
	private OutputStream out;

	public JsonObjectMessageSerializer(IConnectionWrapper connection) throws IOException {
		this.connection = connection;
		out = connection.getOutputStream();
		in = connection.getInputStream();
		jsonMapper = new ObjectMapper();
	}

	@Override
	public void close() throws IOException {
		JavaUtil.closeIgnoreErrors(in, out, connection);
	}

	@Override
	public void serialize(IClipboardMessage message) throws IOException {
		jsonMapper.writeValue(out, message);
	}

	@Override
	public IClipboardMessage deserialize() throws IOException {
		BaseClipboardMessage json = jsonMapper.readValue(in, BaseClipboardMessage.class);
		return null;
	}
}
