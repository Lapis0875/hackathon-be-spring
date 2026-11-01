package com.hackathon.api.auth;

import com.hackathon.api.user.User;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record UserResponse(
		@Schema(requiredMode = REQUIRED) Long id,
		@Schema(requiredMode = REQUIRED) String email) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail());
	}
}
