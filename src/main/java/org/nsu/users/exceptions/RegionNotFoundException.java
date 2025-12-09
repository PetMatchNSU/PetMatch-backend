package org.nsu.users.exceptions;

public class RegionNotFoundException extends RuntimeException {
    public RegionNotFoundException(Long id) {
        super("Region not found: " + id);
    }

    public RegionNotFoundException(String region, String city) {
        super("Region not found: " + region + ", " + city);
    }
}
