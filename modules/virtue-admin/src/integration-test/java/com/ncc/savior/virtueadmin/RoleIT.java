package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-int-test.properties")
public class RoleIT {

	static class Role {
		public List<String> applicationIds;
		public String awsTemplateName;
		public boolean enabled;
		public String id;
		public String lastEditor;
		public Date lastModification;
		public String name;
		public String version;
		public String color;
		public String userCreatedBy;
		public Date timeCreatedAt;
		public List<String> virtualMachineTemplateIds;
	}

	@LocalServerPort
	private int randomServerPort;

	@Before
	public void setup() {
		given().port(randomServerPort).when().get("/data/templates/preload").then().statusCode(HttpStatus.SC_OK);
	}

	@After
	public void tearDown() {
		given().port(randomServerPort).when().get("/data/clear").then().statusCode(HttpStatus.SC_OK);
	}

	@Test
	public void listRolesTest() {
		List<Role> list = given().port(randomServerPort).when().get("/admin/virtue/template").then().extract()
				.jsonPath().getList("", Role.class);
		assertThat(list).isNotEmpty();
	}
}
