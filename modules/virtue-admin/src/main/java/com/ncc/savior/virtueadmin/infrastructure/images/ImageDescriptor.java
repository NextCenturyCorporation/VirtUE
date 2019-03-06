package com.ncc.savior.virtueadmin.infrastructure.images;

public class ImageDescriptor {

	private String templatePath;

	public ImageDescriptor(String templatePath) {
		this.templatePath = templatePath;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	@Override
	public String toString() {
		return "ImageDescriptor [templatePath=" + templatePath + "]";
	}

}
