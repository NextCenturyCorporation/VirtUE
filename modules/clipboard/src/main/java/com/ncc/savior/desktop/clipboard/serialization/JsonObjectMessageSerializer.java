package com.ncc.savior.desktop.clipboard.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.util.JavaUtil;

/**
 * WIP - does not currently work because typically JSON deserialization with
 * jackson requires knowing the exact class you are getting.
 *
 *
 */
public class JsonObjectMessageSerializer implements IMessageSerializer {
	private static final Logger logger = LoggerFactory.getLogger(JsonObjectMessageSerializer.class);
	private ObjectMapper jsonMapper;
	private IConnectionWrapper connection;
	private BufferedReader in;
	private BufferedWriter out;

	public JsonObjectMessageSerializer(IConnectionWrapper connection) throws IOException {
		logger.debug("Json Serializer constructor");
		this.connection = connection;
		out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		jsonMapper = new ObjectMapper();
	}

	@Override
	public void close() throws IOException {
		JavaUtil.closeIgnoreErrors(in, out, connection);
	}

	@Override
	public void serialize(IClipboardMessage message) throws IOException {
		logger.debug("serializing message: " + message);
		String name = message.getClass().getName();
		ObjectNode tree = (ObjectNode) jsonMapper.valueToTree(message);
		tree.put("className", name);
		String stringValue = jsonMapper.writeValueAsString(tree);
		out.write(stringValue);
		out.newLine();
		out.flush();
		logger.debug("flushed message: " + message);
	}

	@Override
	public IClipboardMessage deserialize() throws IOException {
		logger.debug("reading..");
		String line = in.readLine();
		logger.debug("Read line: " + line);
		ObjectNode tree = (ObjectNode) jsonMapper.readTree(line);
		String className = tree.get("className").asText();
		Class<IClipboardMessage> klass = null;
		try {
			klass = (Class<IClipboardMessage>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			logger.error("error getting class name", e);
			return null;
		}
		// BaseClipboardMessage json = jsonMapper.readValue(in,
		// BaseClipboardMessage.class);
		tree.remove("className");
		IClipboardMessage message = jsonMapper.treeToValue(tree, klass);
		return message;
	}
}
