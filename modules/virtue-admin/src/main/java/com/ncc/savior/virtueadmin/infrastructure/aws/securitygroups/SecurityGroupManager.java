package com.ncc.savior.virtueadmin.infrastructure.aws.securitygroups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;

public class SecurityGroupManager {
	private static final Logger logger = LoggerFactory.getLogger(SecurityGroupManager.class);
	private static final String TAG_TEMPLATE_ID = "savior-sg-virtue-template-id";
	private static final String TAG_SERVER_ID = "savior-sg-server-id";
	private static final String TAG_AUTO_GENERATED = "savior-sg-auto-generated";
	private static final String TAG_CREATED_TIME = "savior-sg-created-time";
	private static final String TAG_NAME = "Name";
	private AmazonEC2 ec2;
	private String vpcId;
	private String serverId;

	public SecurityGroupManager(AwsEc2Wrapper ec2Wrapper, String vpcName, String serverId) {
		this.ec2 = ec2Wrapper.getEc2();
		this.vpcId = AwsUtil.getVpcIdFromVpcName(vpcName, ec2Wrapper);
		if (!JavaUtil.isNotEmpty(serverId)) {
			serverId = System.getProperty("user.name");
		}
		this.serverId = serverId;
	}

	public void listSecurityGroups() {
		DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(describeSecurityGroupsRequest);
		List<SecurityGroup> secGs = result.getSecurityGroups();
		for (SecurityGroup sg : secGs) {
			if (vpcId.equals(sg.getVpcId())) {
				if (AwsUtil.tagEquals(sg.getTags(), TAG_SERVER_ID, serverId)) {
					logger.debug("SecurityGroup: " + sg.getGroupName());
					List<IpPermission> ingress = sg.getIpPermissions();
					for (IpPermission ip : ingress) {
						Integer from = ip.getFromPort();
						Integer to = ip.getToPort();
						String protocol = ip.getIpProtocol();
						List<IpRange> ips = ip.getIpv4Ranges();
						logger.debug("   To: " + to + " From: " + from + " Protocol: " + protocol + " IPS: " + ips);
					}
				}
			}
		}
		logger.debug("done");
	}

	/**
	 * Returns the already existing security group id. If non exist, create a new
	 * one. Should never return null.
	 * 
	 * @param templateId
	 * @return
	 */
	public String getSecurityGroupById(String templateId) {
		String groupId = getExistingGroupById(templateId);
		if (groupId == null) {
			String groupName = createGroupName(templateId);
			String description = createGroupDescription();
			groupId = createEmptySecGroup(groupName, description);
			assignGroupToId(groupId, templateId, groupName);
		}
		return groupId;
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
		DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(describeSecurityGroupsRequest);
		List<SecurityGroup> secGs = result.getSecurityGroups();
		for (SecurityGroup sg : secGs) {
			if (vpcId.equals(sg.getVpcId())) {
				List<Tag> tags = sg.getTags();
				boolean idMatch = AwsUtil.tagEquals(tags, TAG_TEMPLATE_ID, templateId);
				boolean serverMatch = AwsUtil.tagEquals(tags, TAG_SERVER_ID, serverId);
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
		tags.add(new Tag(TAG_TEMPLATE_ID, templateId));
		tags.add(new Tag(TAG_SERVER_ID, serverId));
		tags.add(new Tag(TAG_AUTO_GENERATED, "true"));
		// tags.add(new Tag(TAG_USER_CREATED, templateId));
		tags.add(new Tag(TAG_CREATED_TIME, Long.toString(System.currentTimeMillis())));
		tags.add(new Tag(TAG_NAME, name));
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

}
