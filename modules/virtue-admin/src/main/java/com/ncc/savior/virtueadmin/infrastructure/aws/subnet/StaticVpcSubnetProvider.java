package com.ncc.savior.virtueadmin.infrastructure.aws.subnet;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;

/**
 * {@link IVpcSubnetProvider} that uses a single VPC and single subnet and
 * always returns those.
 * 
 *
 */
public class StaticVpcSubnetProvider implements IVpcSubnetProvider {
	private static final Logger logger = LoggerFactory.getLogger(StaticVpcSubnetProvider.class);
	private String subnetId;
	private String vpcId;
	private AwsEc2Wrapper ec2Wrapper;

	public StaticVpcSubnetProvider(AwsEc2Wrapper ec2Wrapper, String vpcName, String subnetName) {
		this.ec2Wrapper = ec2Wrapper;
		getVpcAndSubnetIds(subnetName, vpcName);
	}

	public void getVpcAndSubnetIds(String subnetName, String vpcName) {
		String vpcId = AwsUtil.getVpcIdFromVpcName(vpcName, ec2Wrapper);
		String newSubnetId = AwsUtil.getSubnetIdFromName(vpcId, subnetName, ec2Wrapper);

		if (newSubnetId != null) {
			this.subnetId = newSubnetId;
			this.vpcId = vpcId;
		} else {
			logger.error("Failed to find subnet with name=" + subnetName);
		}
	}

	@Override
	public String getVpcId() {
		return vpcId;
	}

	@Override
	public String getSubnetId(String subnetKey, Map<String, String> tags) {
		return this.subnetId;
	}

	@Override
	public void releaseBySubnetKey(String id) {
		// do nothing
	}
}
