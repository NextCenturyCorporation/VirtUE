/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

/**
 * Implementation of {@link ITemplateService} which uses FreeMarker as the
 * implementation. This implementation utilizes a default/override system for
 * templates. With a given templatePath (constructor arg), the service searchs
 * for templates first in that path on the working directory. If a template
 * cannot be found, it will then search the classpath. Therefore, we can put
 * default files in the classpath/jar file and allow local instances to override
 * using the working directory based templates.
 * 
 *
 */
public class FreeMarkerTemplateService implements ITemplateService {
	private static final Logger logger = LoggerFactory.getLogger(FreeMarkerTemplateService.class);
	private Configuration workingCfg;
	private Configuration classpathCfg;

	public FreeMarkerTemplateService(String templatePath) {
		File workingDirTemplates = new File(templatePath);
		// if (workingDirTemplates.exists()) {
		workingCfg = new Configuration(Configuration.VERSION_2_3_27);
		try {
			workingCfg.setDirectoryForTemplateLoading(workingDirTemplates);
			workingCfg.setDefaultEncoding("UTF-8");
			workingCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			workingCfg.setLogTemplateExceptions(false);
			workingCfg.setWrapUncheckedExceptions(true);
		} catch (FileNotFoundException e) {
			logger.warn("local template directory "+workingDirTemplates.getAbsolutePath()+" not found.  Using default templates only!");
		} catch (IOException e) {
			logger.error("error setting up FreeMarker", e);
		}
		// }
		classpathCfg = new Configuration(Configuration.VERSION_2_3_27);
		classpathCfg.setClassForTemplateLoading(FreeMarkerTemplateService.class, "/templates");
		classpathCfg.setDefaultEncoding("UTF-8");
		classpathCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		classpathCfg.setLogTemplateExceptions(false);
		classpathCfg.setWrapUncheckedExceptions(true);
	}

	public Template getTemplate(String templateName)
			throws MalformedTemplateNameException, ParseException, IOException {
		Template template;
		logger.trace("Getting template with name=" + templateName);
		try {
			template = workingCfg.getTemplate(templateName);
		} catch (TemplateNotFoundException e) {
			logger.trace("Did not find template in working directory with name=" + templateName);
			template = classpathCfg.getTemplate(templateName);
		}
		return template;
	}

	@Override
	public void processTemplate(String templateName, Writer out, Map<String, ?> dataModel)
			throws TemplateException {
		try {
			Template template = getTemplate(templateName);
			template.process(dataModel, out);
		} catch (IOException | freemarker.template.TemplateException e) {
			throw new TemplateException(e);
		}

	}

	@Override
	public String[] processTemplateToLines(String templateName, Map<String, ?> dataModel)
			throws TemplateException {
		StringBuilderWriter out = new StringBuilderWriter();
		processTemplate(templateName, out, dataModel);
		String str = out.toString();
		String[] list = str.split("\\r?\\n");
		return list;
	}

	public static void main(String[] args)
			throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
		ITemplateService ts = new FreeMarkerTemplateService("templates");
		Map<String, Object> dataModel = new HashMap<String, Object>();
		HashMap<String, String> nfs = new HashMap<String, String>();
		nfs.put("internalIpAddress", "1.1.1.1");
		dataModel.put("nfs", nfs);
		String[] lines = ts.processTemplateToLines("windowsStartup.tpl", dataModel);
		for (String line : lines) {
			if (line.trim().startsWith("#")) {
				System.out.println("COMMENT: " + line);

			} else {
				System.out.println("COMMAND: " + line);
			}
		}
	}
}
