package org.nsu.users.exceptions;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String email) {
        super("User profile not found for email: " + email);
    }
}