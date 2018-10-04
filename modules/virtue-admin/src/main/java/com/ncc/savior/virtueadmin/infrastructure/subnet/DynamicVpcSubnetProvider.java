package com.ncc.savior.virtueadmin.infrastructure.subnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateSubnetResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.ModifySubnetAttributeRequest;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.jpa.CidrAssignmentRepository;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.CidrBlockAssignment;

public class DynamicVpcSubnetProvider implements IVpcSubnetProvider {
	private static final Logger logger = LoggerFactory.getLogger(DynamicVpcSubnetProvider.class);
	private AmazonEC2 ec2;
	private String vpcId;
	private CidrBlock startingCidrBlock;
	private CidrBlock endCidrBlock;

	@Autowired
	private CidrAssignmentRepository cidrRepo;
	private Boolean usePublicIp;
	private String routeTableId;
	private String availabilityZone;

	public DynamicVpcSubnetProvider(AwsEc2Wrapper ec2Wrapper, String vpcName, String firstCidrBlock,
			String endNonInclusiveCidrBlock, boolean usePublicIp, String routeTableId, String availabilityZone) {
		this.ec2 = ec2Wrapper.getEc2();
		this.availabilityZone=availabilityZone;
		this.startingCidrBlock = CidrBlock.fromString(firstCidrBlock);
		this.endCidrBlock = CidrBlock.fromString(endNonInclusiveCidrBlock);
		this.usePublicIp = usePublicIp;
		this.routeTableId = routeTableId;
		String vpcId = AwsUtil.getVpcIdFromVpcName(vpcName, ec2Wrapper);
		if (vpcId != null) {
			this.vpcId = vpcId;
		} else {
			logger.error("Failed to find vpc with name=" + vpcName);
		}
	}

	@Override
	public String getSubnetId(String subnetKey, Map<String, String> tags) {
		CidrBlockAssignment assignment = getExistingSubnetId(subnetKey);
		if (assignment == null) {
			assignment = getNextAvailableBlock(subnetKey, tags);
			cidrRepo.save(assignment);
		}
		return assignment.getInfrastructurId();
	}

	private synchronized CidrBlockAssignment getNextAvailableBlock(String subnetKey, Map<String, String> tags) {
		Subnet subnet = createAwsSubnet(startingCidrBlock);

		List<Tag> awsTags = new ArrayList<Tag>();
		for (Entry<String, String> entry : tags.entrySet()) {
			awsTags.add(new Tag(entry.getKey(), entry.getValue()));
		}
		if (!tags.containsKey(IVpcSubnetProvider.TAG_NAME)) {
			String name = tags.get(IVpcSubnetProvider.TAG_USERNAME) + "-"
					+ tags.get(IVpcSubnetProvider.TAG_VIRTUE_NAME);
			awsTags.add(new Tag(IVpcSubnetProvider.TAG_NAME, name));
		}
		CreateTagsRequest createTagsRequest = new CreateTagsRequest(Collections.singletonList(subnet.getSubnetId()),
				awsTags);
		ec2.createTags(createTagsRequest);
		CidrBlockAssignment assignment = new CidrBlockAssignment(subnet.getCidrBlock(), subnetKey,
				tags.get(IVpcSubnetProvider.TAG_USERNAME), subnet.getSubnetId());
		return assignment;
	}

	private Subnet createAwsSubnet(CidrBlock cidrBlock) {
		return createAwsSubnet(cidrBlock, 10);
	}

	private Subnet createAwsSubnet(CidrBlock cidrBlock, int numTries) {
		try {
			CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest(vpcId, cidrBlock.toString());
			createSubnetRequest.withAvailabilityZone(availabilityZone);
			CreateSubnetResult result = ec2.createSubnet(createSubnetRequest);
			Subnet subnet = result.getSubnet();

			if (usePublicIp) {
				ModifySubnetAttributeRequest modifySubnetAttributeRequest = new ModifySubnetAttributeRequest()
						.withMapPublicIpOnLaunch(usePublicIp).withSubnetId(subnet.getSubnetId());
				ec2.modifySubnetAttribute(modifySubnetAttributeRequest);
				AssociateRouteTableRequest associateRouteTableRequest = new AssociateRouteTableRequest()
						.withSubnetId(subnet.getSubnetId()).withRouteTableId(routeTableId);
				ec2.associateRouteTable(associateRouteTableRequest);
			}

			return subnet;
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidSubnet.Range")) {
				throw new SaviorException(SaviorErrorCode.CONFIGURATION_ERROR, "Invalid Subnet range for " + cidrBlock);
			} else if (e.getErrorCode().equals("InvalidSubnet.Conflict")) {
				if (numTries > 0) {
					numTries--;
					CidrBlock nextBlock = CidrBlock.getNextCidrBlock(cidrBlock);
					logger.warn("Failed to create subnet at " + cidrBlock + " due to conflict.  Attempting subnet at "
							+ nextBlock + ".  Retries left=" + numTries);
					// try again
					return createAwsSubnet(nextBlock, numTries);
				} else {
					throw new SaviorException(SaviorErrorCode.AWS_ERROR,
							"Unable to create cidrBlock after many retries " + cidrBlock);
				}
			}
			throw new SaviorException(SaviorErrorCode.AWS_ERROR, "Unable to create Subnet for cidr block=" + cidrBlock,
					e);
		}
	}

	/**
	 * Finds the already created and assigned cidrBlock for the give key or returns
	 * null;
	 * 
	 * @param subnetKey
	 * @return
	 */
	public CidrBlockAssignment getExistingSubnetId(String subnetKey) {
		Optional<CidrBlockAssignment> opt = cidrRepo.findById(subnetKey);
		return opt.isPresent() ? opt.get() : null;
	}

	@Override
	public void releaseSubnetId(String subnetId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getVpcId() {
		return vpcId;
	}

}
