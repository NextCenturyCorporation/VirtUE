package com.ncc.savior.virtueadmin.infrastructure.images;

public class ImageResult {

	private String templatePath;

	public ImageResult(String templatePath) {
		this.templatePath = templatePath;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	@Override
	public String toString() {
		return "ImageResult [templatePath=" + templatePath + "]";
	}

}
