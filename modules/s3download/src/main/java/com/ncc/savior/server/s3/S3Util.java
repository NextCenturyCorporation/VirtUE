package com.ncc.savior.server.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class S3Util {
	public static AWSCredentialsProviderChain getCredentialsProvider() {
		AWSCredentialsProviderChain credentialsProvider = new AWSCredentialsProviderChain(
				new EnvironmentVariableCredentialsProvider(), new SystemPropertiesCredentialsProvider(),
				new ProfileCredentialsProvider("virtue"), new PropertiesFileCredentialsProvider("./aws.properties"),
				new EC2ContainerCredentialsProviderWrapper());
		return credentialsProvider;
	}
	
	public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
			throws IOException {
		long count = 0;
		int n;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
