package com.hackathon.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.hackathon.api.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Issues and parses HS256 JWTs. Claims: sub (user id), email, iat, exp. */
@Component
public class JwtProvider {

	private final SecretKey key;
	private final long expiresInSeconds;

	public JwtProvider(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expires-in-seconds}") long expiresInSeconds) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiresInSeconds = expiresInSeconds;
	}

	public long expiresInSeconds() {
		return expiresInSeconds;
	}

	public String issue(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expiresInSeconds)))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}

	/** @throws JwtException if the token is malformed, tampered with, or expired */
	public AuthenticatedUser parse(String token) throws JwtException {
		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		return new AuthenticatedUser(Long.valueOf(claims.getSubject()), claims.get("email", String.class));
	}
}
