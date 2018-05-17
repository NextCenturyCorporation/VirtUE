// package com.ncc.savior.desktop.clipboard.hub;
//
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// import com.jcraft.jsch.Channel;
// import com.jcraft.jsch.ChannelExec;
// import com.jcraft.jsch.JSchException;
// import com.jcraft.jsch.Session;
// import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
// import com.ncc.savior.desktop.xpra.connection.ssh.JschUtils;
// import
// com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
// import com.ncc.savior.util.SshUtil;
//
// public class SshClipboardConnectionFactory implements IConnectionFactory {
// private static final Logger logger =
// LoggerFactory.getLogger(SshClipboardConnectionFactory.class);
//
// @Override
// public IClipboardConnection createConnection(IConnectionParameters
// connectionParams) {
// if (connectionParams instanceof SshConnectionParameters) {
// SshConnectionParameters params = (SshConnectionParameters) connectionParams;
// try {
// Session session = JschUtils.getSession(params);
// session.connect();
//
// ChannelExec channel = (ChannelExec) session.openChannel("exec");
// String command = getCommand(commandDir, commandName, commandMode,
// p.getDisplay());
// logger.debug("connecting with command=" + command);
// channel.setCommand(command);
// channel.connect();
// InputStream in = channel.getInputStream();
// OutputStream out = channel.getOutputStream();
// return new SshClipboardConection(in, out, channel, session);
// } catch (JSchException e) {
// logger.error("error connecting", e);
// }
// }
// return null;
//
// }
//
// public static class SshClipboardConection implements IClipboardConnection {
// private Session session;
// private Channel channel;
// private OutputStream out;
// private InputStream in;
//
// public SshClipboardConection(InputStream in, OutputStream out, Channel
// channel, Session session) {
// this.in = in;
// this.out = out;
// this.channel = channel;
// this.session = session;
// }
//
// @Override
// public InputStream getInputStream() {
// return in;
// }
//
// @Override
// public OutputStream getOutputStream() {
// return out;
// }
//
// @Override
// public void close() {
// if (in != null) {
// try {
// in.close();
// } catch (IOException e) {
// logger.error("Error closing ssh inputstream", e);
// }
// in = null;
// }
// if (out != null) {
// try {
// out.close();
// } catch (IOException e) {
// logger.error("Error closing ssh outputsream", e);
// }
// out = null;
//
// }
// SshUtil.disconnectLogErrors(session, channel);
//
// }
//
// }
//
// }
