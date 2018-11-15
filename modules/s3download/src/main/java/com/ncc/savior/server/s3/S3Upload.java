package com.ncc.savior.server.s3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;

public class S3Upload {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length==5) {
			uploadencrypted(args);
		}else {
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
			usage("File '"+source.getAbsolutePath()+"' does not exist.");
		}
		client.putObject(bucket, key, source);
	}

	private static void usage(String string) {
		if (string!=null && !string.trim().equals("")) {
			System.out.println("Error: "+string);
		}
		System.out.println("Usage: ");
		System.out.println(
				"  Upload encrypted parameters: <awsRegion> <kmsCmkId> <s3BucketName> <s3ObjectKey> <localDestinationFilePath>");
		System.exit(-1);
	}
	
	

}
