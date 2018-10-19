package com.ncc.savior.virtueadmin.model;

public class SecurityGroupPermission {
	private boolean ingress;
	private Integer fromPort;
	private Integer toPort;
	private String cidrIp;
	private String ipProtocol;
	private String description;
	private String securityGroupId;

	public SecurityGroupPermission(String securityGroupId, boolean ingress, Integer fromPort, Integer toPort, String cidrIp, String ipProtocol,
			String desc) {
		super();
		this.securityGroupId=securityGroupId;
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

	@Override
	public String toString() {
		return "SecurityGroupPermission [ingress=" + ingress + ", fromPort=" + fromPort + ", toPort=" + toPort
				+ ", cidrIp=" + cidrIp + ", ipProtocol=" + ipProtocol + ", description=" + description
				+ ", securityGroupId=" + securityGroupId + "]";
	}

}
