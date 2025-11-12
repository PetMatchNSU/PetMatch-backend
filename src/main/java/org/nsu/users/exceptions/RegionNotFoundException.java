package org.nsu.users.exceptions;

public class RegionNotFoundException extends RuntimeException {
    public RegionNotFoundException(Long id) {
        super("Region not found: " + id);
    }
}