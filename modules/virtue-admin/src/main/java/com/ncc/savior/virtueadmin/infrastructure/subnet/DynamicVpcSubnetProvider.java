package com.ncc.savior.virtueadmin.infrastructure.subnet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CidrBlock;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;

public class DynamicVpcSubnetProvider implements IVpcSubnetProvider {
	private static final Logger logger = LoggerFactory.getLogger(DynamicVpcSubnetProvider.class);
	private AmazonEC2 ec2;
	private String vpcId;

	public DynamicVpcSubnetProvider(AwsEc2Wrapper ec2Wrapper, String vpcName) {
		this.ec2 = ec2Wrapper.getEc2();new CidrBlock().
		this.cidrBlock="10.6.0.0/24";
		String vpcId = AwsUtil.getVpcIdFromVpcName(vpcName, ec2Wrapper);

		if (vpcId != null) {
			this.vpcId = vpcId;
		} else {
			logger.error("Failed to find vpc with name=" + vpcName);
		}
	}

	@Override
	public String getSubnetId(String subnetKey) {
		cidrBlock=getCidrBlock()
		CreateSubnetRequest createSubnetRequest=new CreateSubnetRequest(vpcId, cidrBlock);
		ec2.createSubnet(createSubnetRequest);
	}

	@Override
	public void releaseSubnetId(String subnetId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getVpcId() {
		// TODO Auto-generated method stub
		return null;
	}

}
