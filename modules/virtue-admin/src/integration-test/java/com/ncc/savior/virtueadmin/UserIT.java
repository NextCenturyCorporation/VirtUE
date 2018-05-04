package com.ncc.savior.virtueadmin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

import com.ncc.savior.virtueadmin.RoleIT.Role;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class UserIT {

	static class User {
		public List<String> authorities;
		public String username;
		public List<String> virtueTemplateIds;
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
	public void createUsersTest() {
		List<VirtueTemplate> listOfTemplates = given().port(randomServerPort).when().get("/admin/virtue/template")
				.then().extract().jsonPath().getList("", VirtueTemplate.class);

		User userToPost = new User();
		userToPost.username = "ItTestUser1";
		userToPost.authorities = new ArrayList<String>();
		userToPost.authorities.add("ROLE_USER");
		userToPost.virtueTemplateIds = new ArrayList<String>();
		userToPost.virtueTemplateIds.add(listOfTemplates.get(0).getId());

		ContentType contentType = ContentType.JSON;
		User userReturned = given().port(randomServerPort).when().body(userToPost).contentType(contentType)
				.post("/admin/user").then().extract().as(User.class);

		User user = given().port(randomServerPort).when().get("/admin/user/" + userToPost.username).then().extract()
				.as(User.class);

		assertThat(user).isEqualToComparingFieldByFieldRecursively(userReturned);
		assertThat(user).isEqualToComparingFieldByFieldRecursively(userToPost);
	}

	@Test
	public void listUsersTest() {
		List<User> list = given().port(randomServerPort).when().get("/admin/user").then().extract().jsonPath()
				.getList("", User.class);
		assertThat(list).isNotEmpty();
	}

	@Test
	public void getUserTest() {
		List<User> list = given().port(randomServerPort).when().get("/admin/user").then().extract().jsonPath()
				.getList("", User.class);
		// make sure we can get info about each user and that the info is the same
		for (User user : list) {
			User retrievedUser = given().port(randomServerPort).when().get("/admin/user/{username}", user.username)
					.as(User.class);
			assertThat(user).isEqualToComparingFieldByFieldRecursively(retrievedUser);
		}
	}

	@Test
	public void getBadUserTest() {
		given().port(randomServerPort).when().get("/admin/user/{username}", UUID.randomUUID().toString() + "-bad")
				.then().assertThat().statusCode(400);
	}

	@Test
	public void assignRoleTest() {
		// get users and virtues
		List<User> users = given().port(randomServerPort).when().get("/admin/user").then().extract().jsonPath()
				.getList("", User.class);
		List<Role> roles = given().port(randomServerPort).when().get("/admin/virtue/template").then().extract()
				.jsonPath().getList("", Role.class);
		int numRoles = roles.size();

		// find a user w/o all the roles
		Optional<User> testUser = users.stream().filter(user -> user.virtueTemplateIds.size() < numRoles).findAny();
		assertThat(testUser.isPresent());

		// get a role the user doesn't have already
		User user = testUser.get();
		Optional<Role> testRole = roles.stream().filter(role -> !user.virtueTemplateIds.contains(role.id)).findAny();
		assertThat(testRole.isPresent());

		given().port(randomServerPort).when()
				.post("/admin/user/{username}/assign/{role}", user.username, testRole.get().id).then()
				.statusCode(HttpStatus.SC_NO_CONTENT);
		User retrievedUser = given().port(randomServerPort).when().get("/admin/user/{username}", user.username)
				.as(User.class);
		assertThat(retrievedUser.username).isEqualTo(user.username);
		// did adding the role work?
		assertThat(retrievedUser.virtueTemplateIds).contains(testRole.get().id);
	}

	@Test
	public void revokeRoleTest() {
		User originalUser = given().port(randomServerPort).when().get("/admin/user").then().extract().jsonPath()
				.getList("", User.class).iterator().next();
		String role = originalUser.virtueTemplateIds.iterator().next();
		given().port(randomServerPort).when().post("/admin/user/{username}/revoke/{role}", originalUser.username, role)
				.then().statusCode(HttpStatus.SC_NO_CONTENT);
		User revokedUser = given().port(randomServerPort).when().get("/admin/user/{username}", originalUser.username)
				.as(User.class);
		assertThat(revokedUser.virtueTemplateIds).doesNotContain(role);
	}
}
