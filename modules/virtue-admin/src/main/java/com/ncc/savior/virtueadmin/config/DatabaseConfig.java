package com.ncc.savior.virtueadmin.config;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*
 *  DatabaseConfig is used for configuring the database. 
 */

@EnableTransactionManagement
@EnableJpaRepositories("com.ncc.savior.virtueadmin.repository")
public class DatabaseConfig {
	@Bean
	public SessionFactory sessionFactory(@Qualifier("entityManagerFactory") EntityManagerFactory emf) {
		return emf.unwrap(SessionFactory.class);
	}
}
