package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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

import com.ncc.savior.virtueadmin.UserIT.User;

import io.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class ImportIT {

	@LocalServerPort
	private int randomServerPort;

	@Before
	public void setup() {
		// given().port(randomServerPort).when().get("/data/templates/preload").then().statusCode(HttpStatus.SC_OK);
		given().port(randomServerPort).when().get("/data/import/user/admin").then().statusCode(HttpStatus.SC_OK);
	}

	@After
	public void tearDown() {
		given().port(randomServerPort).when().get("/data/clear").then().statusCode(HttpStatus.SC_OK);
	}

	@Test
	public void listApplicationsTest() {
		String response = given().port(randomServerPort).when().get("/admin/import/all").prettyPrint();
		assertThat(response).isNotEmpty();
		Response resp = given().port(randomServerPort).when().get("/admin/user").thenReturn();
		List<User> list = given().port(randomServerPort).when().get("/admin/user").then().extract().jsonPath()
				.getList("", User.class);
		// at least the 3 from the imports should be there. Depending on settings, there
		// could be more.
		assertThat(list.size()).isGreaterThanOrEqualTo(3);
		assertThat(list).anySatisfy((u) -> {
			assertThat(u).hasFieldOrPropertyWithValue("username", "alice");
		});
		assertThat(list).anySatisfy((u) -> {
			assertThat(u).hasFieldOrPropertyWithValue("username", "bob");
		});
		assertThat(list).anySatisfy((u) -> {
			assertThat(u).hasFieldOrPropertyWithValue("username", "admin");
		});
	}
}
