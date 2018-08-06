package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
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
@TestPropertySource("/application-test.properties")
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
		
		// get all raw permissions
		List<ClipboardPermission> rawPermissions = given().port(randomServerPort).when().get("/data/permissions/raw").then().extract()
				.jsonPath().getList("", ClipboardPermission.class);
		assertThat(rawPermissions).isNotNull();
		
		// get all computed permissions
		Map<Pair, ClipboardPermissionOption> computedPermissions = given().port(randomServerPort).when().get("/data/permissions/computed").then().extract()
				.jsonPath().getMap("", Pair.class, ClipboardPermissionOption.class);
		assertThat(computedPermissions).isNotNull();
	}
	
}
