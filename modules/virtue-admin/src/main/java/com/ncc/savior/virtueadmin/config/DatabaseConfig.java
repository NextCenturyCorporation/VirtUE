/* 
*  DatabaseConfig.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Nov 29, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.config;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*
 *  DatabaseConfig is used for configuring the database. 
 */

@EnableTransactionManagement
@EnableJpaRepositories("com.ncc.savior.virtueadmin.repository")
public class DatabaseConfig {

}
