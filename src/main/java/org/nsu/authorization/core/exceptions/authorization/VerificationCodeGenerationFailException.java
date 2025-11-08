package org.nsu.authorization.core.exceptions.authorization;

public class VerificationCodeGenerationFailException extends RuntimeException {
    public VerificationCodeGenerationFailException(String message) {
        super(message);
    }

    public VerificationCodeGenerationFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
