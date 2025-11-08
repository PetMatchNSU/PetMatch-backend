package org.nsu.authorization.core.exceptions.authorization;

public class EmailVerificationFailException extends RuntimeException {
    public EmailVerificationFailException(String message) {
        super(message);
    }

    public EmailVerificationFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
