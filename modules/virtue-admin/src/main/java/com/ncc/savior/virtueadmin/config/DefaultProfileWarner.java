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
package com.ncc.savior.virtueadmin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Provides a warning to users if they are using the default profile is being
 * used. The default profile should not be used in production but is ok in
 * development. This warning can be enabled simply by using the 'default-alert'
 * spring profile.
 */
@Profile("default-alert")
@Component
public class DefaultProfileWarner {
	private static final Logger logger = LoggerFactory.getLogger(DefaultProfileWarner.class);

	public DefaultProfileWarner() {
		logger.warn("\n\t***WARNING: Using default profile! (This is ok for development)***"
				+ "\n\t*  If you want to use a customized profile, you can set your profiles via:"
				+ "\n\t\t1. Create an application.properties file in your working directory and add a 'spring.profiles.active' property"
				+ "\n\t\t2. Add '--spring.profiles.active=' as a command line arugment"
				+ "\n\t Profile lists can be comma seperated");

	}
}
