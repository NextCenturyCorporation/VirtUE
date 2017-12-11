package com.ncc.savior.virtueadmin.data.jpa;

import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

@SpringBootApplication
@EntityScan("com.ncc.savior.virtueadmin.model")
public class JpaTest {

	private static final Logger log = LoggerFactory.getLogger(JpaTest.class);

	public static void main(String[] args) {
		SpringApplication.run(JpaTest.class);
	}

	@Bean
	public CommandLineRunner demo(VirtueTemplateRepository repository) {
		return (args) -> {
			// save a couple of customers
			ArrayList<VirtualMachineTemplate> vmt = new ArrayList<VirtualMachineTemplate>();
			ArrayList<ApplicationDefinition> apps = new ArrayList<ApplicationDefinition>();
			apps.add(new ApplicationDefinition(UUID.randomUUID().toString(), "testApp", "V1", OS.LINUX));
			vmt.add(new VirtualMachineTemplate(UUID.randomUUID().toString(), "test", OS.LINUX, "myTemplatePath", apps));
			repository.save(new VirtueTemplate(UUID.randomUUID().toString(), "template1", "v1",
					apps, vmt));
//			repository.save(new Customer("Chloe", "O'Brian"));
//			repository.save(new Customer("Kim", "Bauer"));
//			repository.save(new Customer("David", "Palmer"));
//			repository.save(new Customer("Michelle", "Dessler"));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (VirtueTemplate template : repository.findAll()) {
				log.info(template.toString());
			}
			log.info("");

			// fetch an individual customer by ID
			VirtueTemplate virtueTemplate = repository.findOne("id");
			log.info("Customer found with findOne(1L):");
			log.info("--------------------------------");
			log.info(virtueTemplate == null ? "null" : virtueTemplate.toString());
			log.info("");

			// fetch customers by last name
			log.info("Customer found with findByLastName('Bauer'):");
			log.info("--------------------------------------------");
			// for (VirtueTemplate bauer : repository.findByLastName("Bauer")) {
			// log.info(bauer.toString());
			// }
			log.info("");
		};
	}

}