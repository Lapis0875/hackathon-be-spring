package com.hackathon.api.common;

public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException(String email) {
		super(email + " is already registered");
	}
}
