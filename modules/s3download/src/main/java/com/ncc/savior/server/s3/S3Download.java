package com.ncc.savior.server.s3;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Security;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

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
		S3Object obj = encryptionClient.getObject(bucket, key);
		S3ObjectInputStream stream = obj.getObjectContent();
		FileOutputStream out = new FileOutputStream(destinationPath);
		S3Util.copyLarge(stream, out, new byte[4096]);
	}

	private static void usage(String string) {
		if (string!=null && !string.trim().equals("")) {
			System.out.println("Error: "+string);
		}
		System.out.println("Usage: ");
		System.out.println(
				"  Download encrypted parameters: <awsRegion> <kmsCmkId> <s3BucketName> <s3ObjectKey> <localDestinationFilePath>");
		System.out.println(
				"  Download unencrypted parameters: <awsRegion> <s3BucketName> <s3ObjectKey> <localDestinationFilePath>");
		System.exit(-1);
	}
}