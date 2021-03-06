/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.util;

import java.io.ByteArrayOutputStream;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

/**
 * Helper class that creates RSA keypairs mainly for SSH login.
 */
public class RsaKeyGenerator {
	private JSch jsch;

	public RsaKeyGenerator() {
		jsch = new JSch();
	}

	public PublicPrivatePair createRsaKeyPair() throws JSchException {

		KeyPair kpair = null;
		try {
			kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
			ByteArrayOutputStream privOut = new ByteArrayOutputStream();
			ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
			kpair.writePublicKey(pubOut, "");
			byte[] publicBytes = pubOut.toByteArray();
			kpair.writePrivateKey(privOut);
			byte[] privateBytes = privOut.toByteArray();
			return new PublicPrivatePair(privateBytes, publicBytes);
		} finally {
			if (kpair != null) {
				kpair.dispose();
			}
		}
	}

	public class PublicPrivatePair {
		private String privateKey;
		private String publicKey;

		public PublicPrivatePair(byte[] privateKey, byte[] publicKey) {
			this.privateKey = new String(privateKey);
			this.publicKey = new String(publicKey);
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public String getPublicKey() {
			return publicKey;
		}
	}

	public static void main(String[] args) throws JSchException {
		RsaKeyGenerator gen = new RsaKeyGenerator();
		PublicPrivatePair pair = gen.createRsaKeyPair();
		System.out.println("Public:\n" + pair.getPublicKey());
		System.out.println("Private:\n" + pair.getPrivateKey());

		pair = gen.createRsaKeyPair();
		System.out.println("Public:\n" + pair.getPublicKey());
		System.out.println("Private:\n" + pair.getPrivateKey());

		pair = gen.createRsaKeyPair();
		System.out.println("Public:\n" + pair.getPublicKey());
		System.out.println("Private:\n" + pair.getPrivateKey());
	}

	public static void oldMain() {
		int type = KeyPair.RSA;
		// type = KeyPair.DSA;
		String filename = "myRsa";
		String comment = "";

		JSch jsch = new JSch();

		// String passphrase = null;
		// JTextField passphraseField = (JTextField) new JPasswordField(20);
		// Object[] ob = { passphraseField };
		// int result = JOptionPane.showConfirmDialog(null, ob, "Enter passphrase (empty
		// for no passphrase)",
		// JOptionPane.OK_CANCEL_OPTION);
		// if (result == JOptionPane.OK_OPTION) {
		// passphrase = passphraseField.getText();
		// }

		try {
			KeyPair kpair = KeyPair.genKeyPair(jsch, type);
			// kpair.setPassphrase(passphrase); deprecated
			// if (passphrase != null) {
			// kpair.writePrivateKey(filename, passphrase.getBytes());
			// } else {
			kpair.writePrivateKey(filename);
			// }
			kpair.writePublicKey(filename + ".pub", comment);
			System.out.println("Finger print: " + kpair.getFingerPrint());
			kpair.dispose();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.exit(0);

	}

}
