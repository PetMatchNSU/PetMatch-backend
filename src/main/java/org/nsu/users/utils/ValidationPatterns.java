package org.nsu.users.utils;

public final class ValidationPatterns {
    private ValidationPatterns() {}

    /**
     * Pattern matching time in HH:mm 24-hour format (00:00 - 23:59)
     */
    public static final String TIME_HH_MM = "^(?:[01]\\d|2[0-3]):[0-5]\\d$";
    
}