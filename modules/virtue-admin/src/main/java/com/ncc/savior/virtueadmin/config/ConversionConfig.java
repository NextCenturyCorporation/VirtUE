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
*  ConversionConfig.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Nov 29, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;


/**
 * Conversion class
 * 
 * 
 */
@Configuration
public class ConversionConfig {
	
	@SuppressWarnings("rawtypes")
	private Set<Converter> getConverters(){
		Set<Converter> converters = new HashSet<Converter>(); 
		
		return converters; 
	}
	
	public ConversionService conversionService() {
		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean(); 
		bean.setConverters(getConverters());
		bean.afterPropertiesSet();
		
		return bean.getObject();
		
	}

	
}
