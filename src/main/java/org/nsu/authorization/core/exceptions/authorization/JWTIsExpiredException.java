package org.nsu.authorization.core.exceptions.authorization;

import org.nsu.authorization.core.utils.JWTTypes;

public class JWTIsExpiredException extends RuntimeException {
    public JWTIsExpiredException(JWTTypes jwtType) {
        super(String.format("%s is expired", jwtType));
    }
}
