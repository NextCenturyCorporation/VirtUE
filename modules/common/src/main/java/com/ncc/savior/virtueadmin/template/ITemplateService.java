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