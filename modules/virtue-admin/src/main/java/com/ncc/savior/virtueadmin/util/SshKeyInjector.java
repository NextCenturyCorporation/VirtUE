package com.ncc.savior.virtueadmin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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
	public String injectSshKey(VirtualMachine vm) throws IOException {
		// Jsch is not thread safe
		JSch ssh = new JSch();
		PublicPrivatePair keyPair = null;
		String privKey = "";
		File key = null;
		try {
			keyPair = keyGenerator.createRsaKeyPair();
			privKey = keyPair.getPrivateKey();
			String pubKey = keyPair.getPublicKey();
			key = File.createTempFile("test", "");
			FileWriter writer = new FileWriter(key);
			writer.write(vm.getPrivateKey());
			writer.close();
			ssh.addIdentity(key.getAbsolutePath());
			Session session = ssh.getSession(vm.getUserName(), vm.getHostname(), vm.getSshPort());
			session.setConfig("PreferredAuthentications", "publickey");
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
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
			channel.disconnect();
			session.disconnect();

		} catch (JSchException e) {
			throw new IOException("Unable to add ssh key to " + vm.getHostname(), e);
		} finally {
			if (key != null) {
				key.delete();
			}
		}
		return privKey;
	}
}
