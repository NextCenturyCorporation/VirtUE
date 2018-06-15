package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;

import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class ApplicationIT {

	static class Application {
		public String id;
		public String name;
		public String version;
		public String os;
		public String launchCommand;
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
	public void listApplicationsTest() {
		List<Application> list = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);
		assertThat(list).isNotEmpty();
	}

	@Test
	public void getValidApplicationsByIdTest() {
		List<Application> list = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);
		assertThat(list).isNotEmpty();

		String id = list.get(0).id;
		ApplicationDefinition app = given().port(randomServerPort).when().get("/admin/application/" + id).then()
				.extract().as(ApplicationDefinition.class);
		assertThat(app).isNotNull();
		assertThat(app.getId()).isEqualTo(id);
	}

	@Test
	public void getInvalidApplicationsByIdTest() {
		String id = UUID.randomUUID().toString() + "-bad";
		given().port(randomServerPort).when().get("/admin/application/" + id).then().assertThat().statusCode(400);
	}

	@Test
	public void deleteValidApplicationsByIdTest() {
		ApplicationDefinition app1 = new ApplicationDefinition(null, "Test Template", "", OS.LINUX, null);
		ContentType contentType = ContentType.JSON;
		Application application = given().port(randomServerPort).when().body(app1).contentType(contentType)
				.post("/admin/application").then().extract().as(Application.class);

		assertThat(application).isNotNull();
		// verify id was given
		assertThat(application.id).isNotNull();

		List<Application> list = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);
		assertThat(list).isNotEmpty();

		String id = application.id;
		given().port(randomServerPort).when().delete("/admin/application/" + id).then().assertThat().statusCode(204);

		List<Application> list2 = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);
		assertThat(list).isNotEmpty();

		assertThat(list2.size() + 1).isEqualTo(list.size());
		for (Application app2 : list2) {
			assertThat(app2.id).isNotEqualTo(id);
		}
	}

	@Test
	public void createApplicationTest() {

		ApplicationDefinition app = new ApplicationDefinition(null, "Test Template", "", OS.LINUX, null);
		ContentType contentType = ContentType.JSON;
		Application application = given().port(randomServerPort).when().body(app).contentType(contentType)
				.post("/admin/application").then().extract().as(Application.class);

		assertThat(application).isNotNull();
		// verify id was given
		assertThat(application.id).isNotNull();
	}

	@Test
	public void updateApplicationTest() {
		List<Application> list = given().port(randomServerPort).when().get("/admin/application").then().extract()
				.jsonPath().getList("", Application.class);

		Application myApp = list.get(0);

		Application updatedApp = new Application();
		updatedApp.launchCommand = myApp.launchCommand + "-newCmd";
		updatedApp.name = myApp.name + "-newName";
		updatedApp.version = myApp.version + ".1";
		Application updateReturned = given().port(randomServerPort).when().body(updatedApp)
				.contentType(ContentType.JSON).put("/admin/application/" + myApp.id).then().extract()
				.as(Application.class);

		ApplicationDefinition app = given().port(randomServerPort).when().get("/admin/application/" + myApp.id).then()
				.extract().as(ApplicationDefinition.class);
		assertThat(app).isNotNull();
		assertThat(app.getId()).isEqualTo(myApp.id);
		assertThat(app.getLaunchCommand()).isEqualTo(updatedApp.launchCommand);
		assertThat(app.getName()).isEqualTo(updatedApp.name);
		assertThat(app.getOs()).isEqualTo(updatedApp.os);
		assertThat(app.getVersion()).isEqualTo(updatedApp.version);

		assertThat(updateReturned).isNotNull();
		assertThat(updateReturned.id).isEqualTo(myApp.id);
		assertThat(updateReturned.launchCommand).isEqualTo(updatedApp.launchCommand);
		assertThat(updateReturned.name).isEqualTo(updatedApp.name);
		assertThat(updateReturned.os).isEqualTo(updatedApp.os);
		assertThat(updateReturned.version).isEqualTo(updatedApp.version);
	}

}
