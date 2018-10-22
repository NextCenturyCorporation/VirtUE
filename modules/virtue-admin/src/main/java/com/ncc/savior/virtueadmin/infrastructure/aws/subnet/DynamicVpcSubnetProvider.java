package com.ncc.savior.virtueadmin.infrastructure.aws.subnet;

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
import com.amazonaws.services.ec2.model.DeleteSubnetRequest;
import com.amazonaws.services.ec2.model.ModifySubnetAttributeRequest;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.jpa.CidrAssignmentRepository;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.CidrBlock;
import com.ncc.savior.virtueadmin.model.CidrBlockAssignment;

/**
 * {@link IVpcSubnetProvider} implementation that creates and removes dynamic
 * subnets per given id. Subnets are created in AWS as well as references store
 * in the database. When a conflict occurs, AWS wins and the database should be
 * corrected over time.
 * 
 * This class still always returns a single VPC.
 *
 */
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
	private CidrBlock nextCidrBlockToTry;

	public DynamicVpcSubnetProvider(AwsEc2Wrapper ec2Wrapper, String vpcName, String firstCidrBlock,
			String endNonInclusiveCidrBlock, boolean usePublicIp, String routeTableId, String availabilityZone) {
		this.ec2 = ec2Wrapper.getEc2();
		this.availabilityZone = availabilityZone;
		this.startingCidrBlock = CidrBlock.fromString(firstCidrBlock);
		this.nextCidrBlockToTry = startingCidrBlock;
		this.endCidrBlock = CidrBlock.fromString(endNonInclusiveCidrBlock);
		if (startingCidrBlock.greaterOrEqual(endCidrBlock)) {
			throw new SaviorException(SaviorErrorCode.CONFIGURATION_ERROR,
					"Ending CIDR block must be greater than starting CIDR block!  Starting=" + startingCidrBlock
							+ ", Ending=" + endCidrBlock);
		}
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
		logger.debug("existing subnet for key=" + subnetKey + " is " + assignment);
		if (assignment == null) {
			assignment = getNextAvailableBlock(subnetKey, tags);
			try {
				cidrRepo.save(assignment);
			} catch (Exception e) {
				// If the save fails, its most likely because there is a straggling entry in the
				// database. We should clear it (because AWS is king) and save one that reflects
				// AWS.
				logger.debug("Save to database failed, attempted to overwrite", e);
				Iterable<CidrBlockAssignment> col = cidrRepo.findByCidrBlock(assignment.getCidrBlock());
				logger.debug("Deleting " + col);
				cidrRepo.deleteAll(col);
				try {
					cidrRepo.save(assignment);
				} catch (Exception e1) {
					String msg = "Error attempting to create subnet and store in database";
					logger.error(msg, e1);
					throw new SaviorException(SaviorErrorCode.DATABASE_ERROR, msg, e1);
				}
			}
		}
		return assignment.getInfrastructureId();
	}

	private synchronized CidrBlockAssignment getNextAvailableBlock(String subnetKey, Map<String, String> tags) {
		Subnet subnet = createAwsSubnet(nextCidrBlockToTry);
		if (tags == null) {
			tags = Collections.emptyMap();
		}
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
		Subnet subnet = createAwsSubnet(cidrBlock, 10);
		nextCidrBlockToTry = nextCidrBlock(nextCidrBlockToTry);
		return subnet;
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
			logger.debug("created cidrBlock subnet at " + cidrBlock + " " + subnet);
			return subnet;
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidSubnet.Range")) {
				throw new SaviorException(SaviorErrorCode.CONFIGURATION_ERROR, "Invalid Subnet range for " + cidrBlock);
			} else if (e.getErrorCode().equals("InvalidSubnet.Conflict")) {
				if (numTries > 0) {
					numTries--;
					nextCidrBlockToTry = nextCidrBlock(cidrBlock);
					logger.warn("Failed to create subnet at " + cidrBlock + " due to conflict.  Attempting subnet at "
							+ nextCidrBlockToTry + ".  Retries left=" + numTries);
					// try again
					return createAwsSubnet(nextCidrBlockToTry, numTries);
				} else {
					throw new SaviorException(SaviorErrorCode.AWS_ERROR,
							"Unable to create cidrBlock after many retries " + cidrBlock);
				}
			}
			throw new SaviorException(SaviorErrorCode.AWS_ERROR, "Unable to create Subnet for cidr block=" + cidrBlock,
					e);
		}
	}

	private CidrBlock nextCidrBlock(CidrBlock cidrBlock) {
		CidrBlock nextBlock = CidrBlock.getNextCidrBlock(cidrBlock);
		// verify we didn't surpass the end block
		if (nextBlock.greaterOrEqual(endCidrBlock)) {
			nextBlock = startingCidrBlock;
		}
		// Should we check the database? or we do, it might be faster, but
		// alternatively, not checking the database will allow us to fix database
		// problems.
		return nextBlock;
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
	public String getVpcId() {
		return vpcId;
	}

	@Override
	public void releaseBySubnetKey(String id) {
		CidrBlockAssignment cidrAssignment = getExistingSubnetId(id);
		String subnetId = cidrAssignment.getInfrastructureId();
		DeleteSubnetRequest deleteSubnetRequest = new DeleteSubnetRequest(subnetId);
		boolean clearDatabase = true;
		try {
			ec2.deleteSubnet(deleteSubnetRequest);
			logger.debug("released subnet " + id);
		} catch (Exception e) {
			logger.debug("failed to delete subnet from AWS", e);
		}
		if (clearDatabase) {
			try {
				cidrRepo.deleteById(id);
			} catch (Exception e) {
				logger.debug("Failed to delete cidr block from database", e);
			}
		}
	}

}