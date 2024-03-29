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
package com.ncc.savior.virtueadmin.data.jpa;

import java.util.ArrayList;
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
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Test main
 */
// @SpringBootApplication
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
			Set<VirtualMachineTemplate> vmts1 = new HashSet<VirtualMachineTemplate>();
			ArrayList<ApplicationDefinition> apps1 = new ArrayList<ApplicationDefinition>();
			ApplicationDefinition a1 = new ApplicationDefinition(UUID.randomUUID().toString(), "testApp", "V1",
					OS.LINUX, null, null);
			apps1.add(a1);
			boolean enabled = true;
			Date now = new Date();
			String systemUser = "system";
			String loginUser = "admin";
			VirtualMachineTemplate vmt1 = new VirtualMachineTemplate(UUID.randomUUID().toString(), "test", OS.LINUX,
					"myTemplatePath", apps1, loginUser, enabled, now, systemUser);
			vmts1.add(vmt1);
			VirtueTemplate vt1 = new VirtueTemplate(UUID.randomUUID().toString(), "template1", "1", vmts1,
					"default-template", "#C0C0C0", enabled, now, systemUser);

			tm.addApplicationDefinition(a1);
			tm.addVmTemplate(vmt1);
			tm.addVirtueTemplate(vt1);

			VirtueUser user = VirtueUser.testUser();
			// userRep.save(new UserName(user.getUsername()));

			Map<String, VirtueTemplate> testVts = tm.getVirtueTemplatesForUser(user);
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
			for (VirtueTemplate template : vtRepository.findAll()) {
				log.info(template.toString());
			}
			log.info("");

			// fetch an individual customer by ID
			VirtueTemplate virtueTemplate = vtRepository.findById("id").get();
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
