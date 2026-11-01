package com.hackathon.api.auth;

import com.hackathon.api.common.EmailAlreadyRegisteredException;
import com.hackathon.api.security.JwtProvider;
import com.hackathon.api.user.User;
import com.hackathon.api.user.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
	}

	@Transactional
	public UserResponse signup(SignupRequest request) {
		if (users.existsByEmail(request.email())) {
			throw new EmailAlreadyRegisteredException(request.email());
		}
		User user = users.save(new User(request.email(), passwordEncoder.encode(request.password())));
		return UserResponse.from(user);
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		User user = users.findByEmail(request.email())
				.filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		return TokenResponse.bearer(jwtProvider.issue(user), jwtProvider.expiresInSeconds());
	}
}
