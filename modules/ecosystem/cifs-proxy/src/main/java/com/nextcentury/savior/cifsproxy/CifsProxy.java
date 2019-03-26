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
package com.nextcentury.savior.cifsproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.ncc.savior.virtueadmin.template.FreeMarkerTemplateService;

/**
 * Startup file for the CIFS Proxy. It proxies a CIFS/SMB filesystem
 * to a Virtue, applying Virtue-specific permsisions along the
 * way. Details are in the SAVIOR CIFS Proxy document.
 * 
 * @author clong
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class CifsProxy {

	@Value("${savior.cifsproxy.templateDir:templates}")
	private String templateDir;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(CifsProxy.class, args);
	}

	@Bean
	public FreeMarkerTemplateService templateService() {
		return new FreeMarkerTemplateService(templateDir);
	}
}
