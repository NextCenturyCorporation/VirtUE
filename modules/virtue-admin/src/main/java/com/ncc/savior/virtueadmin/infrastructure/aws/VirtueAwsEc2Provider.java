package com.ncc.savior.virtueadmin.infrastructure.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.route53.AmazonRoute53Async;
import com.amazonaws.services.route53.AmazonRoute53AsyncClientBuilder;

/**
 * Provides AWS support classes such as {@link AmazonEC2} or
 * {@link AmazonRoute53Async} using the given region and credentials from the
 * system.
 * 
 *
 */
public class VirtueAwsEc2Provider {
	private static final String PROPERTY_AWS_PROFILE = "aws.profile";
	private static final Logger logger = LoggerFactory.getLogger(VirtueAwsEc2Provider.class);
	private String awsProfile;
	private String region;
	private AWSCredentialsProviderChain credentialsProvider;
	private AmazonEC2 ec2;
	private AmazonRoute53Async route53Client;

	public VirtueAwsEc2Provider(String region, String awsProfile) {
		this.region = region;
		this.awsProfile = awsProfile;
		init();
	}

	/**
	 * initializes AWS/EC2 system mainly getting credentials.
	 * 
	 * @throws AmazonClientException
	 */
	private void init() throws AmazonClientException {
		// Set all AWS credential providers to use the virtue profile
		if (awsProfile != null && !awsProfile.trim().equals("")) {
			System.setProperty(PROPERTY_AWS_PROFILE, awsProfile);
		}
		// use the standard AWS credential provider chain so we can support a bunch of
		// different methods to get credentials.
		credentialsProvider = new AWSCredentialsProviderChain(new EnvironmentVariableCredentialsProvider(),
				new SystemPropertiesCredentialsProvider(), new ProfileCredentialsProvider(awsProfile),
				new PropertiesFileCredentialsProvider("./aws.properties"),
				new EC2ContainerCredentialsProviderWrapper());
		try {
			credentialsProvider.getCredentials();

		} catch (Exception e) {
			logger.warn("Cannot load the credentials from the credential profiles file.  "
					+ "Use CLI to create credentials or add to ./aws.properties file.", e);
		}
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
		route53Client = AmazonRoute53AsyncClientBuilder.standard().withCredentials(credentialsProvider)
				.withRegion(region).build();
	}

	public String getAwsProfile() {
		return awsProfile;
	}

	public String getRegion() {
		return region;
	}

	public AWSCredentialsProviderChain getCredentialsProvider() {
		return credentialsProvider;
	}

	public AmazonEC2 getEc2() {
		return ec2;
	}

	public AmazonRoute53Async getRoute53Client() {
		return route53Client;
	}

}
