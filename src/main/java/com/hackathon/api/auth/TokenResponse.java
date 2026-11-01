package com.hackathon.api.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record TokenResponse(
		@Schema(requiredMode = REQUIRED) String accessToken,
		@Schema(requiredMode = REQUIRED, allowableValues = "Bearer") String tokenType,
		@Schema(requiredMode = REQUIRED, description = "Seconds until expiry") long expiresIn) {

	public static TokenResponse bearer(String accessToken, long expiresIn) {
		return new TokenResponse(accessToken, "Bearer", expiresIn);
	}
}
