package org.nsu.authorization.core.exceptions.authorization;

public class RegionNotFoundException extends RuntimeException {
    public RegionNotFoundException(String message) {
        super(message);
    }
}
