package com.ncc.savior.virtueadmin.template;

import java.io.Writer;
import java.util.Map;

public interface ITemplateService {

	void processTemplate(String templateName, Writer out, Map<String, Object> dataModel)
			throws TemplateException;

	String[] processTemplateToLines(String templateName, Map<String, Object> dataModel)
			throws TemplateException;

	public static class TemplateException extends Exception {
		private static final long serialVersionUID = 1L;
		public TemplateException(Exception e) {
			super(e);
		}
	}
}