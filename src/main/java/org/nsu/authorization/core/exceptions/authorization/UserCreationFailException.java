package org.nsu.authorization.core.exceptions.authorization;

public class UserCreationFailException extends RuntimeException {
    public UserCreationFailException(String message) {
        super(message);
    }
}
