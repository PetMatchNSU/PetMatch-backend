package org.nsu.authorization.core.exceptions.authorization;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(String message) {
        super(message);
    }
}
