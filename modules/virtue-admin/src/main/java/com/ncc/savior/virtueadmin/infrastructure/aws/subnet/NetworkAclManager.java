package com.ncc.savior.virtueadmin.infrastructure.aws.subnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateNetworkAclEntryRequest;
import com.amazonaws.services.ec2.model.CreateNetworkAclRequest;
import com.amazonaws.services.ec2.model.CreateNetworkAclResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteNetworkAclRequest;
import com.amazonaws.services.ec2.model.DescribeNetworkAclsRequest;
import com.amazonaws.services.ec2.model.DescribeNetworkAclsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.IcmpTypeCode;
import com.amazonaws.services.ec2.model.NetworkAcl;
import com.amazonaws.services.ec2.model.NetworkAclAssociation;
import com.amazonaws.services.ec2.model.NetworkAclEntry;
import com.amazonaws.services.ec2.model.ReplaceNetworkAclAssociationRequest;
import com.amazonaws.services.ec2.model.RuleAction;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.CidrBlock;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

/**
 * Creates and manages Network ACLs in the AWS infrastructure. Network ACLs
 * provide security controls between different subnets. The primary goal is to
 * provide a Network ACL that provides isolation between virtues.
 * 
 *
 */
public class NetworkAclManager {
	private static final Logger logger = LoggerFactory.getLogger(NetworkAclManager.class);

	private static final String TAG_PRIMARY_PURPOSE_VIRTUE_ISOLATION = "virtue-isolation";

	private String serverId;
	private AmazonEC2 ec2;
	private String virtueIsolationAclId;
	private String vpcId;
	private CidrBlock endCidr;
	private CidrBlock startCidr;
	private boolean useExistingCidrWithoutChecking = true;

	public NetworkAclManager(ServerIdProvider serverIdProvider, AwsEc2Wrapper ec2Wrapper, String vpcId,
			CidrBlock startingCidrBlock, CidrBlock endCidrBlock) {
		this.serverId = serverIdProvider.getServerId();
		this.ec2 = ec2Wrapper.getEc2();
		this.vpcId = vpcId;
		this.startCidr = startingCidrBlock;
		this.endCidr = endCidrBlock;
		virtueIsolationAclId = getOrGenerateVirtueIsolationAcl();
	}

	private String getOrGenerateVirtueIsolationAcl() {
		Collection<Filter> filters = new ArrayList<Filter>(1);
		filters.add(
				new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_PRIMARY).withValues(TAG_PRIMARY_PURPOSE_VIRTUE_ISOLATION));
		Collection<NetworkAcl> serverAcls = getNetworkAclsForServerId(serverId, filters);
		if (serverAcls.isEmpty()) {
			return createVirtueIsolationAcl();
		} else {
			if (useExistingCidrWithoutChecking) {
				if (serverAcls.size() == 1) {
					NetworkAcl acl = serverAcls.iterator().next();
					logger.info("Using existing Virtue Isolation ACL: " + acl.getNetworkAclId());
					for (NetworkAclEntry entry : acl.getEntries()) {
						logger.info("  " + entry.getRuleNumber() + " " + (entry.getEgress() ? "Outbound" : "Inbound")
								+ " " + entry.getCidrBlock() + " " + entry.getRuleAction() + " "
								+ entry.getIcmpTypeCode() + " " + entry.getProtocol() + " " + entry.getPortRange());
					}
					return acl.getNetworkAclId();
				} else {
					throw new SaviorException(SaviorErrorCode.AWS_ERROR,
							"Multiple Network ACLs setup for isolation for this server ID.  Please check Network ACLs for this server ID="
									+ serverId);
				}
			} else {
				// Test is not yet implemented so we've hardcoded this option away. It would be
				// wise to verify and fix the ACL on startup.
				boolean found = false;
				String isoAcl = null;
				for (NetworkAcl acl : serverAcls) {
					// we are looking for the first correct ACL that is tags as an isolation ACL and
					// deleting the rest
					if (testProperIsolationAcl(acl) && !found) {
						isoAcl = acl.getNetworkAclId();
						found = true;
					} else {
						deleteAcl(acl.getNetworkAclId());
					}
				}
				if (!found) {
					isoAcl = createVirtueIsolationAcl();
				}
				return isoAcl;
			}
		}
	}

	private void deleteAcl(String aclId) {
		try {
			DeleteNetworkAclRequest deleteNetworkAclRequest = new DeleteNetworkAclRequest().withNetworkAclId(aclId);
			ec2.deleteNetworkAcl(deleteNetworkAclRequest);
		} catch (AmazonEC2Exception e) {
			logger.error("Error clearing acl with id=" + aclId, e);
			// should we stop the server from starting if ACLs aren't right? This could
			// happen when there are running virtues and the subnet range for the virtue is
			// changed.
		}
	}

	private boolean testProperIsolationAcl(NetworkAcl acl) {
//		List<String> cidrs = IpToCidrUtil.rangeToCidrList(startCidr.ipToInteger(), endCidr.ipToInteger() - 1);
//		List<NetworkAclEntry> entries = acl.getEntries();
//		for (NetworkAclEntry entry : entries) {
//			String existingCidr = entry.getCidrBlock();
//
//		}
		return false;
	}

	private String createVirtueIsolationAcl() {
		List<String> cidrs = IpToCidrUtil.rangeToCidrList(startCidr.ipToInteger(), endCidr.ipToInteger() - 1);
		CreateNetworkAclRequest createNetworkAclRequest = new CreateNetworkAclRequest();
		createNetworkAclRequest.withVpcId(vpcId);

		CreateNetworkAclResult createResult = ec2.createNetworkAcl(createNetworkAclRequest);
		NetworkAcl virtueIsolationAcl = createResult.getNetworkAcl();
		String isoAclId = virtueIsolationAcl.getNetworkAclId();

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(isoAclId);
		Collection<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(AwsUtil.TAG_SERVER_ID, serverId));
		tags.add(new Tag(AwsUtil.TAG_AUTO_GENERATED, AwsUtil.TAG_AUTO_GENERATED_TRUE));
		tags.add(new Tag(AwsUtil.TAG_CREATED_TIME, "" + System.currentTimeMillis()));
		tags.add(new Tag(AwsUtil.TAG_PRIMARY, TAG_PRIMARY_PURPOSE_VIRTUE_ISOLATION));
		tags.add(new Tag(AwsUtil.TAG_NAME, serverId + "-VirtueIsolation"));
		createTagsRequest.withTags(tags);
		ec2.createTags(createTagsRequest);

		int ruleNumber = 1;
		for (String cidr : cidrs) {
			CreateNetworkAclEntryRequest createNetworkAclEntryRequest = new CreateNetworkAclEntryRequest();
			IcmpTypeCode code = new IcmpTypeCode().withCode(-1);
			createNetworkAclEntryRequest.withEgress(false).withCidrBlock(cidr).withIcmpTypeCode(code);
			createNetworkAclEntryRequest.withNetworkAclId(isoAclId).withRuleNumber(ruleNumber);
			createNetworkAclEntryRequest.withProtocol("-1").withRuleAction(RuleAction.Deny);
			ec2.createNetworkAclEntry(createNetworkAclEntryRequest);
			ruleNumber++;
		}

		String cidr = "0.0.0.0/0";
		IcmpTypeCode code = new IcmpTypeCode().withCode(-1);
		CreateNetworkAclEntryRequest createNetworkAclEntryRequest = new CreateNetworkAclEntryRequest();
		createNetworkAclEntryRequest.withEgress(false).withCidrBlock(cidr).withIcmpTypeCode(code);
		createNetworkAclEntryRequest.withNetworkAclId(isoAclId).withRuleNumber(ruleNumber);
		createNetworkAclEntryRequest.withProtocol("-1").withRuleAction(RuleAction.Allow);
		ec2.createNetworkAclEntry(createNetworkAclEntryRequest);

		createNetworkAclEntryRequest = new CreateNetworkAclEntryRequest();
		createNetworkAclEntryRequest.withEgress(true).withCidrBlock(cidr).withIcmpTypeCode(code);
		createNetworkAclEntryRequest.withNetworkAclId(isoAclId).withRuleNumber(ruleNumber);
		createNetworkAclEntryRequest.withProtocol("-1").withRuleAction(RuleAction.Allow);
		ec2.createNetworkAclEntry(createNetworkAclEntryRequest);
		logger.info("Created VirtueIsolation ACL with id=" + isoAclId);
		return isoAclId;
	}

	private Collection<NetworkAcl> getNetworkAclsForServerId(String serverId, Collection<Filter> additionalFilters) {
		Collection<Filter> filters = new ArrayList<Filter>();
		if (additionalFilters != null) {
			filters.addAll(additionalFilters);
		}
		DescribeNetworkAclsRequest describeNetworkAclsRequest = new DescribeNetworkAclsRequest();
		filters.add(new Filter(AwsUtil.FILTER_VPC_ID).withValues(vpcId));
		filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
		describeNetworkAclsRequest.withFilters(filters);
		DescribeNetworkAclsResult result = ec2.describeNetworkAcls(describeNetworkAclsRequest);
		return result.getNetworkAcls();
	}

	public void associateSubnetWithAcl(Subnet subnet) {
		ReplaceNetworkAclAssociationRequest replaceNetworkAclAssociationRequest = new ReplaceNetworkAclAssociationRequest();
		boolean done = false;
		if (virtueIsolationAclId != null) {
			List<NetworkAcl> acls = ec2.describeNetworkAcls().getNetworkAcls();
			for (NetworkAcl acl : acls) {
				logger.trace(acl.toString());
				List<NetworkAclAssociation> assocs = acl.getAssociations();
				for (NetworkAclAssociation assoc : assocs) {
					logger.trace(assoc.toString());
					if (assoc.getSubnetId().equals(subnet.getSubnetId())) {
						replaceNetworkAclAssociationRequest.withNetworkAclId(virtueIsolationAclId)
								.withAssociationId(assoc.getNetworkAclAssociationId());
						ec2.replaceNetworkAclAssociation(replaceNetworkAclAssociationRequest);
						done = true;
						break;
					}
				}
				if (done) {
					break;
				}
			}
		}
	}
}
