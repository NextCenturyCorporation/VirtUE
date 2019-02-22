package com.ncc.savior.virtueadmin.infrastructure.aws.securitygroups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.SecurityGroupPermission;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

/**
 * Main AWS based implementation of {@link ISecurityGroupManager}. Creates,
 * edits, deletes security groups based on a key which is typically templateID.
 *
 */
public class SecurityGroupManager implements ISecurityGroupManager {
	private static final String FILTER_VPC_ID = "vpc-id";
	private static final String FILTER_GROUP_ID = "group-id";
	private static final Logger logger = LoggerFactory.getLogger(SecurityGroupManager.class);

	private AmazonEC2 ec2;
	private String vpcId;
	private String serverId;

	public SecurityGroupManager(AwsEc2Wrapper ec2Wrapper, ServerIdProvider provider, String vpcName) {
		this.ec2 = ec2Wrapper.getEc2();
		this.vpcId = AwsUtil.getVpcIdFromVpcName(vpcName, ec2Wrapper);
		if (!JavaUtil.isNotEmpty(serverId)) {
			serverId = System.getProperty("user.name");
		}
		this.serverId = provider.getServerId();
	}

	@Override
	public void sync(Collection<String> allTemplateIds) {
		List<SecurityGroup> secGs = getAllSecurityGroupsFromAws();
		Set<String> securityGroupIdsToDelete = new HashSet<String>();
		for (SecurityGroup sg : secGs) {
			if (doesSecurityGroupBelongToThisServer(sg)) {
				List<Tag> tags = sg.getTags();
				String templateId = AwsUtil.tagGet(tags, AwsUtil.TAG_VIRTUE_TEMPLATE_ID);
				if (templateId != null && !allTemplateIds.contains(templateId)) {
					// don't delete anything without the appropriate tags
					securityGroupIdsToDelete.add(sg.getGroupId());
				}
			}
		}
		for (String groupId : securityGroupIdsToDelete) {
			try {
				logger.debug("Attempting to delete extra security group with ID=" + groupId);
				DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest();
				deleteSecurityGroupRequest.withGroupId(groupId);
				ec2.deleteSecurityGroup(deleteSecurityGroupRequest);
			} catch (Exception e) {
				logger.error("Failed to delete security group with id=" + groupId, e);
			}
		}
	}

	public void debugListSecurityGroups() {
		List<SecurityGroup> secGs = getAllSecurityGroupsFromAws();
		for (SecurityGroup sg : secGs) {
			if (doesSecurityGroupBelongToThisServer(sg)) {
				// if (vpcId.equals(sg.getVpcId())) {
				// if (AwsUtil.tagEquals(sg.getTags(), TAG_SERVER_ID, serverId)) {
				logger.debug("SecurityGroup: " + sg.getGroupName());
				List<IpPermission> ingress = sg.getIpPermissions();
				for (IpPermission ip : ingress) {
					Integer from = ip.getFromPort();
					Integer to = ip.getToPort();
					String protocol = ip.getIpProtocol();
					List<IpRange> ips = ip.getIpv4Ranges();
					logger.debug("In   To: " + to + " From: " + from + " Protocol: " + protocol + " IPS: " + ips);
				}
				List<IpPermission> egress = sg.getIpPermissionsEgress();
				for (IpPermission ip : egress) {
					Integer from = ip.getFromPort();
					Integer to = ip.getToPort();
					String protocol = ip.getIpProtocol();
					List<IpRange> ips = ip.getIpv4Ranges();
					logger.debug("Out   To: " + to + " From: " + from + " Protocol: " + protocol + " IPS: " + ips);
				}
			}
		}
		logger.debug("done");
	}

	@Override
	public List<String> getAllSecurityGroupIds() {
		List<SecurityGroup> secGs = getAllSecurityGroupsFromAws();
		List<String> ids = new ArrayList<String>();
		for (SecurityGroup sg : secGs) {
			ids.add(sg.getGroupId());
		}
		return ids;
	}

	@Override
	public Map<String, Collection<SecurityGroupPermission>> getAllSecurityGroupPermissions() {
		List<SecurityGroup> secGs = getAllSecurityGroupsFromAws();
		Map<String, Collection<SecurityGroupPermission>> map = new HashMap<String, Collection<SecurityGroupPermission>>();
		for (SecurityGroup sg : secGs) {
			Collection<SecurityGroupPermission> permissions = securityGroupToPermissionList(sg);
			map.put(sg.getGroupId(), permissions);
		}
		return map;
	}

	@Override
	public Collection<SecurityGroupPermission> getSecurityGroupPermissionsByGroupId(String groupId) {
		if (groupId != null) {
			DescribeSecurityGroupsRequest dsgr = new DescribeSecurityGroupsRequest();
			Collection<Filter> filters = new ArrayList<Filter>();
			filters.add(new Filter(FILTER_GROUP_ID).withValues(groupId));
			dsgr.setFilters(filters);
			DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(dsgr);
			List<SecurityGroup> sgs = result.getSecurityGroups();
			for (SecurityGroup sg : sgs) {
				if (sgs.size() == 1) {
					return securityGroupToPermissionList(sg);
				} else {
					throw new SaviorException(SaviorErrorCode.SECURITY_GROUP_NOT_FOUND,
							"More than one security group with ID=" + groupId + " found.");
				}
			}
		}
		throw new SaviorException(SaviorErrorCode.SECURITY_GROUP_NOT_FOUND,
				"Unable to find security group with ID=" + groupId);
	}

	@Override
	public Collection<SecurityGroupPermission> getSecurityGroupPermissionsByTemplateId(String templateId) {
		if (templateId != null) {
			DescribeSecurityGroupsRequest dsgr = new DescribeSecurityGroupsRequest();
			Collection<Filter> filters = new ArrayList<Filter>();
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_VIRTUE_TEMPLATE_ID).withValues(templateId));
			filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
			dsgr.setFilters(filters);
			DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(dsgr);
			List<SecurityGroup> sgs = result.getSecurityGroups();
			for (SecurityGroup sg : sgs) {
				if (doesSecurityGroupBelongToThisServer(sg)
						&& AwsUtil.tagEquals(sg.getTags(), AwsUtil.TAG_VIRTUE_TEMPLATE_ID, templateId)) {
					if (sgs.size() == 1) {
						return securityGroupToPermissionList(sg);
					} else {
						throw new SaviorException(SaviorErrorCode.SECURITY_GROUP_NOT_FOUND,
								"More than one security group with template ID=" + templateId + " found.");
					}
				}
			}
			List<SecurityGroupPermission> defaultPermissions = new ArrayList<SecurityGroupPermission>();
			defaultPermissions.add(new SecurityGroupPermission(false, null, null, "0.0.0.0/0", "-1", "default"));
			return defaultPermissions;
		}
		throw new SaviorException(SaviorErrorCode.SECURITY_GROUP_NOT_FOUND,
				"Unable to find security group matching template ID=" + templateId);
	}

	@Override
	public String getSecurityGroupIdByTemplateId(String templateId) {
		String groupId = getExistingGroupById(templateId);
		if (groupId == null) {
			String groupName = createGroupName(templateId);
			String description = createGroupDescription();
			groupId = createEmptySecGroup(groupName, description);
			assignGroupToId(groupId, templateId, groupName);
		}
		return groupId;
	}

	@Override
	public void authorizeSecurityGroup(String groupId, SecurityGroupPermission permission) {
		try {
			Integer fromPort = permission.getFromPort();
			Integer toPort = permission.getToPort();
			String cidrIp = permission.getCidrIp();
			String ipProtocol = permission.getIpProtocol();
			if (permission.isIngress()) {
				// AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
				// new AuthorizeSecurityGroupIngressRequest();
				// authorizeSecurityGroupIngressRequest.withGroupId(groupId).withFromPort(fromPort).withToPort(toPort)
				// .withCidrIp(cidrIp).withIpProtocol(ipProtocol);

				AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
				// authorizeSecurityGroupEgressRequest.withGroupId(groupId).withFromPort(fromPort).withToPort(toPort)
				// .withCidrIp(cidrIp).withIpProtocol(ipProtocol);
				IpPermission ipPermission = new IpPermission();
				IpRange ipv4Range = new IpRange();
				ipv4Range.setCidrIp(cidrIp);
				ipv4Range.setDescription(permission.getDescription());
				ipPermission.withIpProtocol(ipProtocol).withFromPort(fromPort).withToPort(toPort)
						.withIpv4Ranges(ipv4Range);
				authorizeSecurityGroupIngressRequest.withIpPermissions(ipPermission).withGroupId(groupId);

				ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
			} else {
				AuthorizeSecurityGroupEgressRequest authorizeSecurityGroupEgressRequest = new AuthorizeSecurityGroupEgressRequest();
				// authorizeSecurityGroupEgressRequest.withGroupId(groupId).withFromPort(fromPort).withToPort(toPort)
				// .withCidrIp(cidrIp).withIpProtocol(ipProtocol);
				IpPermission ipPermission = new IpPermission();
				IpRange ipv4Range = new IpRange();
				ipv4Range.setCidrIp(cidrIp);
				ipv4Range.setDescription(permission.getDescription());
				ipPermission.withIpProtocol(ipProtocol).withFromPort(fromPort).withToPort(toPort)
						.withIpv4Ranges(ipv4Range);
				authorizeSecurityGroupEgressRequest.withIpPermissions(ipPermission).withGroupId(groupId);
				ec2.authorizeSecurityGroupEgress(authorizeSecurityGroupEgressRequest);
			}
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidPermission.Duplicate")) {
				// log and ignore
				logger.debug("Attempted to authorize rule that already existed.  " + e.getLocalizedMessage());
			} else {
				throw new SaviorException(SaviorErrorCode.AWS_ERROR, "Unknown AWS Error: " + e.getLocalizedMessage(),
						e);
			}
		}
	}

	@Override
	public void revokeSecurityGroup(String groupId, SecurityGroupPermission permission) {
		try {
			Integer fromPort = permission.getFromPort();
			Integer toPort = permission.getToPort();
			String cidrIp = permission.getCidrIp();
			String ipProtocol = permission.getIpProtocol();
			if (permission.isIngress()) {
				RevokeSecurityGroupIngressRequest revokeSecurityGroupIngressRequest = new RevokeSecurityGroupIngressRequest();
				revokeSecurityGroupIngressRequest.withGroupId(groupId).withFromPort(fromPort).withToPort(toPort)
						.withCidrIp(cidrIp).withIpProtocol(ipProtocol);
				ec2.revokeSecurityGroupIngress(revokeSecurityGroupIngressRequest);
			} else {
				RevokeSecurityGroupEgressRequest revokeSecurityGroupEgressRequest = new RevokeSecurityGroupEgressRequest();
				IpPermission ipPermission = new IpPermission();
				IpRange ipv4Range = new IpRange();
				ipv4Range.setCidrIp(cidrIp);
				ipPermission.withIpProtocol(ipProtocol).withFromPort(fromPort).withToPort(toPort)
						.withIpv4Ranges(ipv4Range);
				revokeSecurityGroupEgressRequest.withIpPermissions(ipPermission).withGroupId(groupId);
				ec2.revokeSecurityGroupEgress(revokeSecurityGroupEgressRequest);
			}
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidPermission.NotFound")) {
				// log and ignore
				logger.debug("Attempted to revoke rule that did not exist.  " + e.getLocalizedMessage());
			} else {
				throw new SaviorException(SaviorErrorCode.AWS_ERROR, "Unknown AWS Error: " + e.getLocalizedMessage(),
						e);
			}
		}
	}

	@Override
	public void removeSecurityGroup(String groupId) {
		try {
			DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest()
					.withGroupId(groupId);
			ec2.deleteSecurityGroup(deleteSecurityGroupRequest);
		} catch (AmazonEC2Exception e) {
			throw new SaviorException(SaviorErrorCode.AWS_ERROR, "Unknown AWS Error: " + e.getLocalizedMessage(), e);
		}
	}

	private List<SecurityGroup> getAllSecurityGroupsFromAws() {
		DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
		Collection<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter(FILTER_VPC_ID).withValues(vpcId));
		filters.add(new Filter(AwsUtil.FILTER_TAG + AwsUtil.TAG_SERVER_ID).withValues(serverId));
		describeSecurityGroupsRequest.setFilters(filters);
		DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(describeSecurityGroupsRequest);
		List<SecurityGroup> secGs = result.getSecurityGroups();
		return secGs;
	}

	/**
	 * Returns true if the security group has the required tags and values to
	 * indicate that it was auto generated and owned by this server.
	 *
	 * @param sg
	 * @return
	 */
	private boolean doesSecurityGroupBelongToThisServer(SecurityGroup sg) {
		return vpcId.equals(sg.getVpcId()) && (AwsUtil.tagEquals(sg.getTags(), AwsUtil.TAG_SERVER_ID, serverId))
				&& AwsUtil.tagEquals(sg.getTags(), AwsUtil.TAG_AUTO_GENERATED, AwsUtil.TAG_AUTO_GENERATED_TRUE);
	}

	private Collection<SecurityGroupPermission> securityGroupToPermissionList(SecurityGroup sg) {
		HashSet<SecurityGroupPermission> permissions = new HashSet<SecurityGroupPermission>();
		String templateId = AwsUtil.tagGet(sg.getTags(), AwsUtil.TAG_VIRTUE_TEMPLATE_ID);

		for (IpPermission p : sg.getIpPermissions()) {
			boolean ingress = true;
			Integer fromPort = p.getFromPort();
			Integer toPort = p.getToPort();
			String ipProtocol = p.getIpProtocol();
			for (IpRange r : p.getIpv4Ranges()) {
				String cidrIp = r.getCidrIp();
				String desc = r.getDescription();
				SecurityGroupPermission sgp = new SecurityGroupPermission(ingress, fromPort, toPort, cidrIp, ipProtocol,
						desc);
				sgp.setSecurityGroupId(sg.getGroupId());
				sgp.setTemplateId(templateId);
				permissions.add(sgp);
			}
		}
		for (IpPermission p : sg.getIpPermissionsEgress()) {
			boolean ingress = false;
			Integer fromPort = p.getFromPort();
			Integer toPort = p.getToPort();
			String ipProtocol = p.getIpProtocol();
			for (IpRange r : p.getIpv4Ranges()) {
				String cidrIp = r.getCidrIp();
				String desc = r.getDescription();
				SecurityGroupPermission sgp = new SecurityGroupPermission(ingress, fromPort, toPort, cidrIp, ipProtocol,
						desc);
				sgp.setSecurityGroupId(sg.getGroupId());
				sgp.setTemplateId(templateId);
				permissions.add(sgp);
			}
		}
		return permissions;
	}

	private String createGroupDescription() {
		return "Automatically generated security group for server=" + serverId;
	}

	private String createGroupName(String templateId) {
		return "auto-" + serverId + "-" + templateId;
	}

	/**
	 * Returns null if no existing security group is found.
	 *
	 * @param templateId
	 * @return
	 */
	private String getExistingGroupById(String templateId) {
		List<SecurityGroup> secGs = getAllSecurityGroupsFromAws();
		for (SecurityGroup sg : secGs) {
			if (doesSecurityGroupBelongToThisServer(sg)) {
				List<Tag> tags = sg.getTags();
				boolean idMatch = AwsUtil.tagEquals(tags, AwsUtil.TAG_VIRTUE_TEMPLATE_ID, templateId);
				boolean serverMatch = AwsUtil.tagEquals(tags, AwsUtil.TAG_SERVER_ID, serverId);
				if (idMatch && serverMatch) {
					return sg.getGroupId();
				}
			}
		}
		return null;
	}

	private void assignGroupToId(String groupId, String templateId, String name) {

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(groupId);

		Collection<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(AwsUtil.TAG_VIRTUE_TEMPLATE_ID, templateId));
		tags.add(new Tag(AwsUtil.TAG_SERVER_ID, serverId));
		tags.add(new Tag(AwsUtil.TAG_AUTO_GENERATED, AwsUtil.TAG_AUTO_GENERATED_TRUE));
		// tags.add(new Tag(TAG_USER_CREATED, templateId));
		tags.add(new Tag(AwsUtil.TAG_CREATED_TIME, Long.toString(System.currentTimeMillis())));
		tags.add(new Tag(AwsUtil.TAG_NAME, name));
		createTagsRequest.withTags(tags);
		ec2.createTags(createTagsRequest);
	}

	private String createEmptySecGroup(String groupName, String description) {
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest(groupName, description);
		createSecurityGroupRequest.setVpcId(vpcId);
		CreateSecurityGroupResult result = ec2.createSecurityGroup(createSecurityGroupRequest);
		String groupId = result.getGroupId();
		return groupId;
	}

	public static void main(String[] args) {
		String awsProfile = "virtue";
		String region = "us-east-1";
		ServerIdProvider sip = new ServerIdProvider(null);
		VirtueAwsEc2Provider ec2Provider = new VirtueAwsEc2Provider(region, awsProfile);
		AwsEc2Wrapper ec2Wrapper = new AwsEc2Wrapper(ec2Provider, sip, "true");
		String vpcName = "VIRTUE";

		SecurityGroupManager sgm = new SecurityGroupManager(ec2Wrapper, sip, vpcName);

		sgm.debugListSecurityGroups();
		String groupId = sgm.getSecurityGroupIdByTemplateId("test-sg");
		logger.debug("generated group=" + groupId);
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(true, 10000, 10020, "192.168.0.1/32", "tcp", "a"));
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(true, 10000, 10020, "192.168.0.4/32", "tcp", "b"));
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(true, 10000, 10020, "192.168.0.10/32", "tcp", "c"));
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(false, 10000, 10022, "192.168.0.2/32", "tcp", "d"));
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(true, 10003, 10020, "192.168.0.3/32", "tcp", "e"));
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(false, 10000, 10024, "192.168.0.4/32", "udp", "f"));
		sgm.authorizeSecurityGroup(groupId,
				new SecurityGroupPermission(true, 10005, 10020, "192.168.0.5/32", "udp", "g"));
		// sgm.authorizeSecurityGroup(true, groupId, 10005, 10020, "192.168.0.5/32",
		// "icmp");
		logger.debug("added authorizations to group");
		sgm.debugListSecurityGroups();
		sgm.revokeSecurityGroup(groupId,
				new SecurityGroupPermission(false, 10000, 10022, "192.168.0.2/32", "tcp", null));
		sgm.revokeSecurityGroup(groupId,
				new SecurityGroupPermission(true, 10003, 10020, "192.168.0.3/32", "tcp", null));
		logger.debug("removed authorizations to group");
		logger.debug(sgm.getSecurityGroupPermissionsByGroupId(groupId).toString());
		try {
			logger.debug(sgm.getSecurityGroupPermissionsByGroupId("Asaf").toString());
		} catch (Exception e) {
			logger.debug("expected exception");
			// expected
		}
		sgm.debugListSecurityGroups();
		logger.debug("list");
		sgm.sync(new ArrayList<String>());
		logger.debug("attempted to sync and deltete");
	}

}
