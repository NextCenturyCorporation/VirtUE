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
import java.io.IOException;
import java.security.Security;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * Minimalistic application used to upload files to s3 with encrypted
 * transmission and storage.
 *
 */
public class S3Upload {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length == 5) {
			uploadencrypted(args);
		} else {
			usage("Invalid number of parameters");
		}
	}

	public static void uploadencrypted(String[] args) throws FileNotFoundException, IOException {
		Security.setProperty("crypto.policy", "unlimited");

		String region = args[0];
		String kmsCmkId = args[1];
		String bucket = args[2];
		String key = args[3];
		String sourcePath = args[4];

		AWSCredentialsProviderChain credentialsProvider = S3Util.getCredentialsProvider();

		KMSEncryptionMaterialsProvider materialProvider = new KMSEncryptionMaterialsProvider(kmsCmkId);
		CryptoConfiguration cryptoConfig = new CryptoConfiguration().withAwsKmsRegion(RegionUtils.getRegion(region));
		AmazonS3 encryptionClient = AmazonS3EncryptionClientBuilder.standard().withCredentials(credentialsProvider)
				.withEncryptionMaterials(materialProvider).withCryptoConfiguration(cryptoConfig).withRegion(region)
				.build();

		upload(bucket, key, sourcePath, encryptionClient);
	}

	private static void upload(String bucket, String key, String sourcePath, AmazonS3 client)
			throws FileNotFoundException, IOException {

		File source = new File(sourcePath);
		if (!source.exists()) {
			usage("File '" + source.getAbsolutePath() + "' does not exist.");
		}
		// maximum upload size without multipart is 5GB.
		TransferManager tm = TransferManagerBuilder.standard().withS3Client(client).build();
		Upload upload = tm.upload(bucket, key, source);
		upload.addProgressListener(new ProgressListener() {
			long totalTransferred = 0;

			@Override
			public void progressChanged(ProgressEvent progressEvent) {

				if (progressEvent.getEventType().equals(ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT)) {
					totalTransferred += progressEvent.getBytesTransferred();
				} else {
					System.out.println(
							"Progress: " + progressEvent.getEventType() + " Total Transfered: " + totalTransferred);
				}
			}
		});
		try {
			upload.waitForCompletion();
		} catch (AmazonClientException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			tm.shutdownNow(true);
		}
	}

	private static void usage(String string) {
		if (string != null && !string.trim().equals("")) {
			System.out.println("Error: " + string);
		}
		System.out.println("Usage: ");
		System.out.println(
				"  Upload encrypted parameters: <awsRegion> <kmsCmkId> <s3BucketName> <s3ObjectKey> <localDestinationFilePath>");
		System.exit(-1);
	}

}
