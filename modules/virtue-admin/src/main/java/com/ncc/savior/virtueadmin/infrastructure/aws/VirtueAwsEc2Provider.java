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
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.route53.AmazonRoute53Async;
import com.amazonaws.services.route53.AmazonRoute53AsyncClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3Encryption;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;

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
	private AmazonS3 s3;

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
		s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
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

	public AmazonS3 getS3() {
		return s3;
	}

	public AmazonRoute53Async getRoute53Client() {
		return route53Client;
	}

	public AmazonS3Encryption getS3EncryptionClient(String kmsCmkId) {
		KMSEncryptionMaterialsProvider materialProvider = new KMSEncryptionMaterialsProvider(kmsCmkId);
		CryptoConfiguration cryptoConfig = new CryptoConfiguration().withAwsKmsRegion(RegionUtils.getRegion(region));
		AmazonS3Encryption encryptionClient = AmazonS3EncryptionClientBuilder.standard()
				.withCredentials(new ProfileCredentialsProvider()).withEncryptionMaterials(materialProvider)
				.withCryptoConfiguration(cryptoConfig).withRegion(region).build();
		return encryptionClient;
	}
}
