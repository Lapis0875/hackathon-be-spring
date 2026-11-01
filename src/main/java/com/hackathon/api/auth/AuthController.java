package com.hackathon.api.auth;

import com.hackathon.api.common.ProblemDetailSchema;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirements // public: no Bearer required
public class AuthController {

	private static final String PROBLEM = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponse(responseCode = "201", description = "Created")
	@ApiResponse(responseCode = "400", description = "Validation error (errors[] present)",
			content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetailSchema.class)))
	@ApiResponse(responseCode = "409", description = "Email already registered",
			content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetailSchema.class)))
	public UserResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.signup(request);
	}

	@PostMapping("/login")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "401", description = "Bad credentials",
			content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetailSchema.class)))
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
