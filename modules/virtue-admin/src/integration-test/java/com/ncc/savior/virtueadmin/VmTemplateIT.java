package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.ncc.savior.virtueadmin.ApplicationIT.Application;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;

import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class VmTemplateIT {

	@LocalServerPort
	private int randomServerPort;

	@Before
	public void setup() {
		given().port(randomServerPort).when().get("/data/templates/preload").then().statusCode(HttpStatus.SC_OK);
	}

	@Test
	public void listVmTemplateTest() {
		List<VirtualMachineTemplate> list = given().port(randomServerPort).when().get("/admin/virtualMachine/template")
				.then().extract().jsonPath().getList("", VirtualMachineTemplate.class);
		assertThat(list).isNotEmpty();
	}

	@Test
	public void getValidVmTemplatesByIdTest() {
		List<VirtualMachineTemplate> list = given().port(randomServerPort).when().get("/admin/virtualMachine/template")
				.then().extract().jsonPath().getList("", VirtualMachineTemplate.class);
		assertThat(list).isNotEmpty();

		String id = list.get(0).getId();
		VirtualMachineTemplate vmt = given().port(randomServerPort).when().get("/admin/virtualMachine/template/" + id)
				.then().extract().as(VirtualMachineTemplate.class);
		assertThat(vmt).isNotNull();
		assertThat(vmt.getId()).isEqualTo(id);
	}

	// @Test
	// TODO need to fix database integrity constraint violation
	public void deleteVmTemplatesByIdTest() {
		List<Application> appList = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);

		Date initialDate = new Date(0);
		String initialUser = "testUser1234";
		VirtualMachineTemplate vmt1 = new VirtualMachineTemplate(null, "Test Template", OS.LINUX,
				UUID.randomUUID().toString(), null, "test", true, initialDate, initialUser);
		Collection<String> appIds = new ArrayList<String>();
		for (int i = 0; i <= appList.size() / 2; i++) {
			appIds.add(appList.get(i).id);
		}
		vmt1.setApplicationIds(appIds);
		ContentType contentType = ContentType.JSON;
		VirtualMachineTemplate newVmt = given().port(randomServerPort).when().body(vmt1).contentType(contentType)
				.post("/admin/virtualMachine/template/").then().extract().as(VirtualMachineTemplate.class);
		// above is really just to get an ID of a vm template that does belong to any
		// virtues and thus avoid any database constraints

		List<VirtualMachineTemplate> list = given().port(randomServerPort).when().get("/admin/virtualMachine/template")
				.then().extract().jsonPath().getList("", VirtualMachineTemplate.class);
		assertThat(list).isNotEmpty();

		String id = vmt1.getId();
		given().port(randomServerPort).when().delete("/admin/virtualMachine/template/" + id).then().assertThat()
				.statusCode(204);

		List<VirtualMachineTemplate> list2 = given().port(randomServerPort).when().get("/admin/virtualMachine/template")
				.then().extract().jsonPath().getList("", VirtualMachineTemplate.class);
		assertThat(list).isNotEmpty();

		assertThat(list2.size() + 1).isEqualTo(list.size());
		for (VirtualMachineTemplate vmt : list2) {
			assertThat(vmt.getId()).isNotEqualTo(id);
		}
	}

	@Test
	public void getInvalidVmTemplatesByIdTest() {
		String id = UUID.randomUUID().toString() + "-bad";
		given().port(randomServerPort).when().get("/admin/virtualMachine/template/" + id).then().assertThat()
				.statusCode(400);
	}

	@Test
	public void createVmTemplateTest() {
		List<Application> list = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);

		Date initialDate = new Date(0);
		String initialUser = "testUser1234";
		VirtualMachineTemplate vmt = new VirtualMachineTemplate(null, "Test Template", OS.LINUX,
				UUID.randomUUID().toString(), null, "test", true, initialDate, initialUser);
		Collection<String> appIds = new ArrayList<String>();
		for (int i = 0; i <= list.size() / 2; i++) {
			appIds.add(list.get(i).id);
		}
		vmt.setApplicationIds(appIds);
		ContentType contentType = ContentType.JSON;
		VirtualMachineTemplate newVmt = given().port(randomServerPort).when().body(vmt).contentType(contentType)
				.post("/admin/virtualMachine/template/").then().extract().as(VirtualMachineTemplate.class);

		assertThat(newVmt).isNotNull();
		// verify date was updated
		assertThat(newVmt.getLastModification()).isNotEqualTo(initialDate);
		// verify last modication user was updated
		assertThat(newVmt.getLastEditor()).isNotEqualTo(initialUser);
		// verify id was given
		assertThat(newVmt.getId()).isNotNull();
	}

}
