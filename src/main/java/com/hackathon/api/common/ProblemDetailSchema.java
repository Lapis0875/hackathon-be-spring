package com.hackathon.api.common;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * OpenAPI-only description of the RFC 9457 error body every 4xx/5xx returns
 * (the runtime object is Spring's {@link org.springframework.http.ProblemDetail}).
 * Referenced from {@code @ApiResponse} annotations; never instantiated.
 */
@Schema(name = "ProblemDetail", description = "RFC 9457 error body (application/problem+json)")
public record ProblemDetailSchema(
		@Schema(example = "about:blank") String type,
		@Schema(example = "Email already registered") String title,
		@Schema(example = "409") Integer status,
		@Schema(example = "a@b.com is already registered") String detail,
		@Schema(example = "/api/auth/signup") String instance,
		@Schema(description = "Present on 400 validation errors only") List<FieldError> errors) {

	public record FieldError(
			@Schema(requiredMode = REQUIRED, example = "password") String field,
			@Schema(requiredMode = REQUIRED, example = "size must be between 8 and 72") String message) {
	}
}
