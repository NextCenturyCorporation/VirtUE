package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class VirtueTemplateIT {

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
	public void listVirtueTemplateTest() {
		List<VirtueTemplate> list = given().port(randomServerPort).when().get("/admin/virtue/template").then().extract()
				.jsonPath().getList("", VirtueTemplate.class);
		assertThat(list).isNotEmpty();
	}

	@Test
	public void getValidVirtueTemplatesByIdTest() {
		List<VirtueTemplate> list = given().port(randomServerPort).when().get("/admin/virtue/template").then().extract()
				.jsonPath().getList("", VirtueTemplate.class);
		assertThat(list).isNotEmpty();

		String id = list.get(0).getId();
		VirtueTemplate vmt = given().port(randomServerPort).when().get("/admin/virtue/template/" + id).then().extract()
				.as(VirtueTemplate.class);
		assertThat(vmt).isNotNull();
		assertThat(vmt.getId()).isEqualTo(id);
	}

	@Test
	public void getInvalidVirtueTemplatesByIdTest() {
		String id = UUID.randomUUID().toString() + "-bad";
		given().port(randomServerPort).when().get("/admin/virtue/template/" + id).then().assertThat().statusCode(400);
	}

	@Test
	public void createVirtueTemplateTest() {
		List<VirtualMachineTemplate> vmtList = given().port(randomServerPort).when()
				.get("/admin/virtualMachine/template").then().extract().jsonPath()
				.getList("", VirtualMachineTemplate.class);
		assertThat(vmtList).isNotEmpty();

		Date initialDate = new Date(0);
		String initialUser = "testUser1234";

		VirtueTemplate vt = new VirtueTemplate((String) null, "Test Template", "v1", (VirtualMachineTemplate) null,
				UUID.randomUUID().toString(), true, initialDate, initialUser);
		Collection<String> vmtIds = new ArrayList<String>();
		for (int i = 0; i <= vmtList.size() / 2; i++) {
			vmtIds.add(vmtList.get(i).getId());
		}
		vt.setVirtualMachineTemplateIds(vmtIds);
		ContentType contentType = ContentType.JSON;
		VirtueTemplate newVt = given().port(randomServerPort).when().body(vt).contentType(contentType)
				.post("/admin/virtue/template/").then().extract().as(VirtueTemplate.class);

		assertThat(newVt).isNotNull();
		// verify date was updated
		assertThat(newVt.getLastModification()).isNotEqualTo(initialDate);
		// verify last modication user was updated
		assertThat(newVt.getLastEditor()).isNotEqualTo(initialUser);
		// verify id was given
		assertThat(newVt.getId()).isNotNull();

		Collection<String> newVmtIds = newVt.getVirtualMachineTemplateIds();
		for (String id : newVmtIds) {
			assertThat(vmtIds).contains(id);
		}
		assertThat(vmtIds.size()).isEqualTo(newVmtIds.size());
	}

	@Test
	// TODO need to fix database integrity constraint violation
	public void deleteVirtueTemplatesByIdTest() {
		List<VirtualMachineTemplate> vmtList = given().port(randomServerPort).when()
				.get("/admin/virtualMachine/template").then().extract().jsonPath()
				.getList("", VirtualMachineTemplate.class);
		assertThat(vmtList).isNotEmpty();

		Date initialDate = new Date(0);
		String initialUser = "testUser1234";

		VirtueTemplate vt1 = new VirtueTemplate((String) null, "Test Template", "v1", (VirtualMachineTemplate) null,
				UUID.randomUUID().toString(), true, initialDate, initialUser);
		Collection<String> vmtIds = new ArrayList<String>();
		for (int i = 0; i <= vmtList.size() / 2; i++) {
			vmtIds.add(vmtList.get(i).getId());
		}
		vt1.setVirtualMachineTemplateIds(vmtIds);
		ContentType contentType = ContentType.JSON;
		VirtueTemplate newVt = given().port(randomServerPort).when().body(vt1).contentType(contentType)
				.post("/admin/virtue/template/").then().extract().as(VirtueTemplate.class);
		assertThat(newVt).isNotNull();
		// above is really just to get an ID of a virtue template that does belong to
		// any users and thus avoid any database constraints

		List<VirtueTemplate> list = given().port(randomServerPort).when().get("/admin/virtue/template").then().extract()
				.jsonPath().getList("", VirtueTemplate.class);
		assertThat(list).isNotEmpty();

		String id = newVt.getId();
		given().port(randomServerPort).when().delete("/admin/virtue/template/" + id).then().assertThat()
				.statusCode(204);

		List<VirtueTemplate> list2 = given().port(randomServerPort).when().get("/admin/virtue/template").then()
				.extract().jsonPath().getList("", VirtueTemplate.class);
		assertThat(list).isNotEmpty();

		assertThat(list2.size() + 1).isEqualTo(list.size());
		for (VirtueTemplate vt : list2) {
			assertThat(vt.getId()).isNotEqualTo(id);
		}
	}

}
