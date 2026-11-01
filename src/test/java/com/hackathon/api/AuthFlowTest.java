package com.hackathon.api;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowTest {

	private static final String EMAIL = "a@b.com";
	private static final String PASSWORD = "secret123";
	private static final String CREDENTIALS = """
			{ "email": "%s", "password": "%s" }""".formatted(EMAIL, PASSWORD);

	@Autowired
	MockMvc mvc;

	@Autowired
	ObjectMapper json;

	@Test
	@Order(1)
	void signup_returns201WithUser() throws Exception {
		mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value(EMAIL));
	}

	@Test
	@Order(2)
	void signup_duplicateEmail_returns409Problem() throws Exception {
		mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Email already registered"))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value(EMAIL + " is already registered"))
				.andExpect(jsonPath("$.instance").value("/api/auth/signup"));
	}

	@Test
	@Order(3)
	void login_thenMe_returnsUser() throws Exception {
		MvcResult login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(86400))
				.andReturn();
		String token = json.readTree(login.getResponse().getContentAsString()).get("accessToken").asString();

		mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value(EMAIL));
	}

	@Test
	@Order(4)
	void me_withoutToken_returns401Problem() throws Exception {
		mvc.perform(get("/api/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().doesNotExist("WWW-Authenticate"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.instance").value("/api/me"));
	}

	@Test
	@Order(5)
	void login_wrongPassword_returns401Problem() throws Exception {
		String body = """
				{ "email": "%s", "password": "wrong-password" }""".formatted(EMAIL);
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(header().doesNotExist("WWW-Authenticate"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	@Order(6)
	void signup_invalidBody_returns400WithErrors() throws Exception {
		String body = """
				{ "email": "not-an-email", "password": "short" }""";
		mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors", hasSize(2)))
				.andExpect(jsonPath("$.errors[*].field").exists())
				.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@Order(7)
	void openApi_exposesContractSchemas() throws Exception {
		mvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.schemas.SignupRequest").exists())
				.andExpect(jsonPath("$.components.schemas.LoginRequest").exists())
				.andExpect(jsonPath("$.components.schemas.TokenResponse").exists())
				.andExpect(jsonPath("$.components.schemas.UserResponse.required").value(containsInAnyOrder("id", "email")))
				.andExpect(jsonPath("$.components.schemas.TokenResponse.required").value(containsInAnyOrder("accessToken", "tokenType", "expiresIn")))
				.andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.errors").exists())
				.andExpect(jsonPath("$.paths['/api/auth/signup'].post.responses['201'].content['application/json']").exists())
				.andExpect(jsonPath("$.paths['/api/auth/signup'].post.responses['409'].content['application/problem+json'].schema['$ref']").value("#/components/schemas/ProblemDetail"))
				.andExpect(jsonPath("$.paths['/api/me'].get.responses['401'].content['application/problem+json']").exists());
	}
}
