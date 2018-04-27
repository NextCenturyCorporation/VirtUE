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
