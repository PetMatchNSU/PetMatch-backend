package org.nsu.authorization.core.exceptions.authorization;

public class PersonHasNotVerifiedEmailException extends RuntimeException {
    public PersonHasNotVerifiedEmailException(String message) {
        super(message);
    }
}
