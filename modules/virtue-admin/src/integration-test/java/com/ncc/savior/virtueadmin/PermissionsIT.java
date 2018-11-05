package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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

import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-int-test.properties")
public class PermissionsIT {
	
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
	public void permissionsEndpointTest() {	
		// get all computed permissions
		List<ClipboardPermission> computedPermissions = given().port(randomServerPort).when().get("/admin/permissions").then().extract()
				.jsonPath().getList("", ClipboardPermission.class);
		assertThat(computedPermissions).isNotNull();
		
		ClipboardPermission permission = computedPermissions.get(0);
		String sourceId = permission.getSourceGroupId();
		String destinationId = permission.getDestinationGroupId();
		
		// set permission
		String setPermission = given().port(randomServerPort).when().body("ALLOW")
				.post("/admin/permission/" + sourceId + "/" + destinationId).then().extract().asString();
		assertThat(setPermission).isNotNull();
		
		// get all raw permissions
		List<ClipboardPermission> rawPermissions = given().port(randomServerPort).when().get("/admin/permissions?raw=true").then().extract()
				.jsonPath().getList("", ClipboardPermission.class);
		assertThat(rawPermissions).isNotNull();
		
		// check set permission
		ClipboardPermission perm = given().port(randomServerPort).when().get("/admin/permission/" + sourceId + "/" + destinationId).then().extract()
				.as(ClipboardPermission.class);
		assertThat(perm).isNotNull();
		assertTrue(perm.getPermission() == ClipboardPermissionOption.ALLOW);
		
		// delete
		given().port(randomServerPort).when().delete("/admin/permission/" + sourceId + "/" + destinationId);
		
		// check deleted permission
		ClipboardPermission deletedPermission = given().port(randomServerPort).when().get("/admin/permission/" + sourceId + "/" + destinationId).then().extract()
				.as(ClipboardPermission.class);
		assertThat(deletedPermission).isNotNull();
	}
	
}
