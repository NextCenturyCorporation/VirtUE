package com.ncc.savior.virtueadmin.model;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A domain object for a single security group permission. This maps to AWS
 * IpPermission. Each security group can have many Security Group Permissions.
 * 
 *
 */
@JsonInclude(Include.NON_NULL)
@Schema(description = "Network permission for a given virtue")
public class SecurityGroupPermission {
	public static final Comparator<? super SecurityGroupPermission> TEMPLATE_ID_COMPARATOR = new TemplateIdComparator();
	@Schema(description = "True if the security group pertains to connections coming into into the virtue, false if the connection is outgoing.")
	private boolean ingress;
	@Schema(description = "Port the connection came from.")
	private Integer fromPort;
	@Schema(description = "Port the connection came to.")
	private Integer toPort;
	@Schema(description = "CIDR block of that the permission should pertain to.")
	private String cidrIp;
	@Schema(description = "Protocol for.  Usually TCP or UDP.")
	private String ipProtocol;
	@Schema(description = "Human readable description of the purpose of this security group.")
	private String description;
	@Schema(description = "ID of the security group.")
	private String securityGroupId;
	@Schema(description = "Virtue template ID that this permission pertains to.")
	private String templateId;

	// for jackson deserialization only
	@SuppressWarnings("unused")
	private SecurityGroupPermission() {

	}

	public SecurityGroupPermission(boolean ingress, Integer fromPort, Integer toPort, String cidrIp, String ipProtocol,
			String desc) {
		super();
		this.ingress = ingress;
		this.fromPort = fromPort;
		this.toPort = toPort;
		this.cidrIp = cidrIp;
		this.ipProtocol = ipProtocol;
		this.description = desc;
	}

	public boolean isIngress() {
		return ingress;
	}

	public Integer getFromPort() {
		return fromPort;
	}

	public Integer getToPort() {
		return toPort;
	}

	public String getCidrIp() {
		return cidrIp;
	}

	public String getIpProtocol() {
		return ipProtocol;
	}

	public String getDescription() {
		return description;
	}

	public String getSecurityGroupId() {
		return securityGroupId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public String getKey() {
		return Integer.toString(this.hashCode(), 16);
	}

	protected void setIngress(boolean ingress) {
		this.ingress = ingress;
	}

	protected void setFromPort(Integer fromPort) {
		this.fromPort = fromPort;
	}

	protected void setToPort(Integer toPort) {
		this.toPort = toPort;
	}

	protected void setCidrIp(String cidrIp) {
		this.cidrIp = cidrIp;
	}

	protected void setIpProtocol(String ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	public void setSecurityGroupId(String securityGroupId) {
		this.securityGroupId = securityGroupId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	@Override
	public String toString() {
		return "SecurityGroupPermission [ingress=" + ingress + ", fromPort=" + fromPort + ", toPort=" + toPort
				+ ", cidrIp=" + cidrIp + ", ipProtocol=" + ipProtocol + ", description=" + description
				+ ", securityGroupId=" + securityGroupId + ", templateId=" + templateId + "]";
	}

	private static class TemplateIdComparator implements Comparator<SecurityGroupPermission> {
		@Override
		public int compare(SecurityGroupPermission o1, SecurityGroupPermission o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getTemplateId(), o2.getTemplateId());
		}
	}
}
