package com.ncc.savior.virtueadmin.data.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

/**
 * Test main
 */
//@SpringBootApplication
@EntityScan("com.ncc.savior.virtueadmin.model")
public class JpaTest {

	private static final Logger log = LoggerFactory.getLogger(JpaTest.class);

	public static void main(String[] args) {
		SpringApplication.run(JpaTest.class);
	}

	@Bean
	public CommandLineRunner demo(VirtueTemplateRepository vtRepository, VirtualMachineTemplateRepository vmtRepository,
			ApplicationDefinitionRepository appRepository, UserRepository userRep) {
		return (args) -> {
			System.out.println("**************************************");
			SpringJpaTemplateManager tm = new SpringJpaTemplateManager(vtRepository, vmtRepository, appRepository,
					userRep);

			// save a couple of customers
			Set<JpaVirtualMachineTemplate> vmts1 = new HashSet<JpaVirtualMachineTemplate>();
			ArrayList<ApplicationDefinition> apps1 = new ArrayList<ApplicationDefinition>();
			ApplicationDefinition a1 = new ApplicationDefinition(UUID.randomUUID().toString(), "testApp", "V1",
					OS.LINUX);
			apps1.add(a1);
			boolean enabled = true;
			Date now = new Date();
			String systemUser = "system";
			String loginUser = "admin";
			JpaVirtualMachineTemplate vmt1 = new JpaVirtualMachineTemplate(UUID.randomUUID().toString(), "test",
					OS.LINUX,
					"myTemplatePath", apps1, loginUser, enabled, now, systemUser);
			vmts1.add(vmt1);
			JpaVirtueTemplate vt1 = new JpaVirtueTemplate(UUID.randomUUID().toString(), "template1", "v1", vmts1,
					"default-template", enabled, now, systemUser);

			tm.addApplicationDefinition(a1);
			tm.addVmTemplate(vmt1);
			tm.addVirtueTemplate(vt1);

			Collection<String> authorities = new ArrayList<String>();
			authorities.add("ROLE_USER");
			authorities.add("ROLE_ADMIN");
			JpaVirtueUser user = new JpaVirtueUser("Test", authorities);
			// userRep.save(new UserName(user.getUsername()));

			Map<String, JpaVirtueTemplate> testVts = tm.getVirtueTemplatesForUser(user);
			log.info(testVts.toString());

			tm.assignVirtueTemplateToUser(user, vt1.getId());
			testVts = tm.getVirtueTemplatesForUser(user);
			log.info(testVts.toString());

			// repository.save(new Customer("Chloe", "O'Brian"));
			// repository.save(new Customer("Kim", "Bauer"));
			// repository.save(new Customer("David", "Palmer"));
			// repository.save(new Customer("Michelle", "Dessler"));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (JpaVirtueTemplate template : vtRepository.findAll()) {
				log.info(template.toString());
			}
			log.info("");

			// fetch an individual customer by ID
			JpaVirtueTemplate virtueTemplate = vtRepository.findById("id").get();
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