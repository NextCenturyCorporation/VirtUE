package com.ncc.savior.virtueadmin.template;

import java.io.Writer;
import java.util.Map;

/**
 * Templating service for creating commands and scripts with templates based on
 * a given dataModel. Templates and data models are somewhat implementation
 * dependant.
 *
 */
public interface ITemplateService {

	/**
	 * Processes the given template and writes output to given writer.
	 * 
	 * @param templateName
	 * @param out
	 * @param dataModel
	 * @throws TemplateException
	 */
	void processTemplate(String templateName, Writer out, Map<String, Object> dataModel) throws TemplateException;

	/**
	 * Processes the given template and returns in the format of an Array of lines
	 * of the response.
	 * 
	 * @param templateName
	 * @param dataModel
	 * @return
	 * @throws TemplateException
	 */
	String[] processTemplateToLines(String templateName, Map<String, Object> dataModel) throws TemplateException;

	public static class TemplateException extends Exception {
		private static final long serialVersionUID = 1L;

		public TemplateException(Exception e) {
			super(e);
		}
	}
}