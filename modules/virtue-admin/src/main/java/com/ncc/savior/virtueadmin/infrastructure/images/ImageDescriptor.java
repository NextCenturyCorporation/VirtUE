package com.ncc.savior.virtueadmin.infrastructure.images;

import java.util.List;

public class ImageDescriptor {
	public static final String GNOME_TERMINAL = "gnome-terminal";
	public static final String XTERM = "xterm";
	public static final String NAUTILUS = "nautilus";
	public static final String GNOME_CALCULATOR = "gnome-calculator";
	public static final String LIBREOFFICE = "libreoffice";
	public static final String THUNDERBIRD = "thunderbird";
	public static final String FIREFOX = "firefox";
	
	//apps we should add
	protected static final String CHROME = "";
	protected static final String TERMINATOR = "";

	private String templatePath;
	private String baseLinuxAmi;
	private String dom0Ami;
	private String baseDomUAmi;
	private List<String> appKeys;

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

	public List<String> getAppKeys() {
		return appKeys;
	}

	public void setAppKeys(List<String> appKeys) {
		this.appKeys = appKeys;
	}
}
