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
package com.ncc.savior.server.s3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Security;
import java.util.List;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Minimalistic application to pull down encrypted S3 images.
 *
 */
public class S3Download {

	public static void main(String[] args) throws IOException {
		if (args.length == 4) {
			downloadUnencrypted(args);
		} else if (args.length == 5) {
			downloadEncrypted(args);
		} else {
			usage("Invalid number of parameters");
		}
	}

	public static void downloadUnencrypted(String[] args) throws FileNotFoundException, IOException {
		String region = args[0];
		String bucket = args[1];
		String key = args[2];
		String destinationPath = args[3];

		AWSCredentialsProviderChain credentialsProvider = S3Util.getCredentialsProvider();
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
		download(bucket, key, destinationPath, s3);
	}

	public static void downloadEncrypted(String[] args) throws IOException {
		Security.setProperty("crypto.policy", "unlimited");

		String region = args[0];
		String kmsCmkId = args[1];
		String bucket = args[2];
		String key = args[3];
		String destinationPath = args[4];

		AWSCredentialsProviderChain credentialsProvider = S3Util.getCredentialsProvider();

		KMSEncryptionMaterialsProvider materialProvider = new KMSEncryptionMaterialsProvider(kmsCmkId);
		CryptoConfiguration cryptoConfig = new CryptoConfiguration().withAwsKmsRegion(RegionUtils.getRegion(region));
		AmazonS3 encryptionClient = AmazonS3EncryptionClientBuilder.standard().withCredentials(credentialsProvider)
				.withEncryptionMaterials(materialProvider).withCryptoConfiguration(cryptoConfig).withRegion(region)
				.build();

		download(bucket, key, destinationPath, encryptionClient);
	}

	private static void download(String bucket, String key, String destinationPath, AmazonS3 encryptionClient)
			throws FileNotFoundException, IOException {
		if (key.trim().endsWith("/")) {
			// download folder
			File destination = new File(destinationPath);
			if (destination.isFile()) {
				throw new IllegalArgumentException(
						"key represents a directory, but destination a file.  Cannot copy a directory to a file.");
			}

			ObjectListing objListing = encryptionClient.listObjects(bucket, key);
			List<S3ObjectSummary> oss = objListing.getObjectSummaries();
			for (S3ObjectSummary os : oss) {
				String myKey = os.getKey();
				if (!myKey.endsWith("/")) {
					// file
					String filename = myKey;
					int slashIndex = myKey.lastIndexOf("/");
					if (slashIndex > -1) {
						filename = filename.substring(slashIndex + 1);
					}
					System.out.println("Downloading key " + myKey + " to " + filename);
					download(bucket, myKey, destination + File.separator + filename, encryptionClient);
				}
			}
		} else {
			// download file
			S3Object obj = encryptionClient.getObject(bucket, key);
			S3ObjectInputStream stream = obj.getObjectContent();
			FileOutputStream out = new FileOutputStream(destinationPath);
			S3Util.copyLarge(stream, out, new byte[4096]);
		}

	}

	private static void usage(String string) {
		if (string != null && !string.trim().equals("")) {
			System.out.println("Error: " + string);
		}
		System.out.println("Usage: ");
		System.out.println(
				"  Download encrypted parameters: <awsRegion> <kmsCmkId> <s3BucketName> <s3ObjectKey> <localDestinationFilePath>");
		System.out.println(
				"  Download unencrypted parameters: <awsRegion> <s3BucketName> <s3ObjectKey> <localDestinationFilePath>");
		System.exit(-1);
	}
}
