package com.ncc.savior.virtueadmin.model;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SecurityGroupPermission {
	public static final Comparator<? super SecurityGroupPermission> TEMPLATE_ID_COMPARATOR = new TemplateIdComparator();
	private boolean ingress;
	private Integer fromPort;
	private Integer toPort;
	private String cidrIp;
	private String ipProtocol;
	private String description;
	private String securityGroupId;
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

	public void setIngress(boolean ingress) {
		this.ingress = ingress;
	}

	public Integer getFromPort() {
		return fromPort;
	}

	public void setFromPort(Integer fromPort) {
		this.fromPort = fromPort;
	}

	public Integer getToPort() {
		return toPort;
	}

	public void setToPort(Integer toPort) {
		this.toPort = toPort;
	}

	public String getCidrIp() {
		return cidrIp;
	}

	public void setCidrIp(String cidrIp) {
		this.cidrIp = cidrIp;
	}

	public String getIpProtocol() {
		return ipProtocol;
	}

	public void setIpProtocol(String ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSecurityGroupId() {
		return securityGroupId;
	}

	public void setSecurityGroupId(String securityGroupId) {
		this.securityGroupId = securityGroupId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public String getKey() {
		return Integer.toString(this.hashCode(), 16);
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
