package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class VmIT {
	
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
	public void vmEndpointTest() {
		// get virtue templates so we can create one
		List<VirtueTemplate> templates = given().port(randomServerPort).when().get("/user/virtue/template").then()
				.extract().jsonPath().getList("", VirtueTemplate.class);
		assertThat(templates).isNotEmpty();
		String templateId = templates.get(0).getId();

		// create a virtue instance.
		VirtueInstance instance = given().port(randomServerPort).when().post("/user/virtue/template/" + templateId)
				.then().extract().as(VirtueInstance.class);
		assertThat(instance).isNotNull();

		// check on instance
		instance = given().port(randomServerPort).when().get("/user/virtue/" + instance.getId()).then().extract()
				.as(VirtueInstance.class);
		assertThat(instance).isNotNull();

		// testing getAllVms
		Iterable<VirtualMachine> vms = given().port(randomServerPort).when().get("/admin/vm/").then()
				.extract().jsonPath().getList("", VirtualMachine.class);
		assertThat(vms).isNotNull();
		
		// testing getVm
		VirtualMachine vm = given().port(randomServerPort).when().get("/admin/vm/" + instance.getId())
				.then().extract().as(VirtualMachine.class);
		assertThat(vm).isNotNull();
		
		// delete instance
		given().port(randomServerPort).when().delete("/user/virtue/" + instance.getId()).then().assertThat()
				.statusCode(204);
	}
	
}