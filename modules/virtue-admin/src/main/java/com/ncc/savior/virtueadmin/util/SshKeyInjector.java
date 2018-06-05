package com.ncc.savior.virtueadmin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.RsaKeyGenerator.PublicPrivatePair;

/**
 * Helper class that uses an {@link RsaKeyGenerator} to create a new RSA keypair
 * and inject the public key into the 'authorized_keys" file of the SSH config
 * on a given {@link VirtualMachine}. The {@link VirtualMachine} must already
 * have existing credentials to login via SSH for the injection to work.
 */
public class SshKeyInjector {
	private static final Logger logger = LoggerFactory.getLogger(SshKeyInjector.class);
	private RsaKeyGenerator keyGenerator;

	public SshKeyInjector() {
		keyGenerator = new RsaKeyGenerator();

	}

	/**
	 * returns new private key used to login to machine
	 * 
	 * @param vm
	 * @return
	 * @throws IOException
	 */
	public String injectSshKey(VirtualMachine vm, String privateKey) throws IOException {
		File privateKeyFile = null;
		try {
			privateKeyFile = File.createTempFile("test", "");
			FileWriter writer = new FileWriter(privateKeyFile);
			writer.write(privateKey);
			writer.close();
			return injectSshKey(vm, privateKeyFile);
		} finally {
			if (privateKeyFile != null) {
				privateKeyFile.delete();
			}
		}
	}

	/**
	 * returns new private key used to login to machine
	 * 
	 * @param vm
	 * @return
	 * @throws IOException
	 */
	public String injectSshKey(VirtualMachine vm, File privateKeyFile) throws IOException {
		PublicPrivatePair keyPair = null;
		String privKey = "";
		// Jsch is not thread safe
		Session session = null;
		ChannelExec channel = null;
		try {
			keyPair = keyGenerator.createRsaKeyPair();
			privKey = keyPair.getPrivateKey();
			String pubKey = keyPair.getPublicKey();
			session = SshUtil.getConnectedSession(vm, privateKeyFile);
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("echo '" + pubKey + "' >> ~/.ssh/authorized_keys");
			channel.connect();

			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			InputStreamReader estream = new InputStreamReader(channel.getErrStream());
			BufferedReader ereader = new BufferedReader(estream);
			String line;
			while ((line = reader.readLine()) != null || (line = ereader.readLine()) != null) {
				if (logger.isTraceEnabled()) {
					logger.trace(line);
				}
			}
			return privKey;
		} catch (JSchException e) {
			throw new IOException("Unable to add ssh key to " + vm.getHostname(), e);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}
}
