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
/*
*  ApiConfig.java
*
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.config;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

import com.ncc.savior.virtueadmin.rest.AdminResource;
import com.ncc.savior.virtueadmin.rest.DataResource;
import com.ncc.savior.virtueadmin.rest.DesktopRestService;
import com.ncc.savior.virtueadmin.rest.HelloResource;
import com.ncc.savior.virtueadmin.rest.UserResource;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

/**
 * ApiConfig is responsible for registering all the webserivce apis.
 *
 *
 */
@Component
@ApplicationPath("/")
@PropertySources({ @PropertySource(value = "classpath:savior-server.properties", ignoreResourceNotFound = true),
		@PropertySource(value = "file:savior-server.properties", ignoreResourceNotFound = true) })
public class ApiConfig extends ResourceConfig {

	public ApiConfig() {

		//register(ContextInitializer.class);

		/* Register all you webservice class here: */
		register(DesktopRestService.class);
		register(DataResource.class);
		register(HelloResource.class);
		register(AdminResource.class);
		register(UserResource.class);
		register(WebServiceUtil.class);
		register(MultiPartFeature.class);
		register(CorsFilter.class);
		property(ServletProperties.FILTER_FORWARD_ON_404, true);


	}

}
