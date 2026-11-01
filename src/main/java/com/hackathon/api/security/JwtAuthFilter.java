package com.hackathon.api.security;

import java.io.IOException;
import java.util.List;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads "Authorization: Bearer <jwt>", and on success sets an {@link AuthenticatedUser} principal.
 * Invalid tokens are ignored here; the request then fails as anonymous (401 via the entry point).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String BEARER = "Bearer ";

	private final JwtProvider jwtProvider;

	public JwtAuthFilter(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER)) {
			try {
				AuthenticatedUser user = jwtProvider.parse(header.substring(BEARER.length()));
				var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, List.of());
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			catch (JwtException | IllegalArgumentException ignored) {
				// anonymous
			}
		}
		chain.doFilter(request, response);
	}
}
