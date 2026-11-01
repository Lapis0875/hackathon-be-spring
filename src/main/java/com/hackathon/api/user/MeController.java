package com.hackathon.api.user;

import com.hackathon.api.auth.UserResponse;
import com.hackathon.api.common.ProblemDetailSchema;
import com.hackathon.api.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/me", produces = MediaType.APPLICATION_JSON_VALUE)
public class MeController {

	@GetMapping
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "401", description = "Missing/invalid/expired token",
			content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
					schema = @Schema(implementation = ProblemDetailSchema.class)))
	public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
		return new UserResponse(principal.id(), principal.email());
	}
}
