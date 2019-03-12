package com.ncc.savior.virtueadmin.infrastructure.images;

public class ImageDescriptor {

	private String templatePath;
	private String baseLinuxAmi;
	private String dom0Ami;
	private String baseDomUAmi;

	public ImageDescriptor(String templatePath) {
		this.templatePath = templatePath;
	}

	public ImageDescriptor() {

	}

	public String getTemplatePath() {
		return templatePath;
	}

	@Override
	public String toString() {
		return "ImageDescriptor [templatePath=" + templatePath + "]";
	}

	public String getBaseLinuxAmi() {
		return baseLinuxAmi;
	}

	public void setBaseLinuxAmi(String baseLinuxAmi) {
		this.baseLinuxAmi = baseLinuxAmi;
	}

	public String getDom0Ami() {
		return dom0Ami;
	}

	public void setDom0Ami(String dom0Ami) {
		this.dom0Ami = dom0Ami;
	}

	public String getBaseDomUAmi() {
		return baseDomUAmi;
	}

	public void setBaseDomUAmi(String baseDomUAmi) {
		this.baseDomUAmi = baseDomUAmi;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
}
