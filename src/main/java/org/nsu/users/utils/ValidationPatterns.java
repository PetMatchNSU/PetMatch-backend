package org.nsu.users.utils;

public final class ValidationPatterns {
    private ValidationPatterns() {}

    /**
     * Pattern matching time in HH:mm 24-hour format (00:00 - 23:59)
     */
    public static final String TIME_HH_MM = "^(?:[01]\\d|2[0-3]):[0-5]\\d$";

    /**
     * Jackson/DateTime pattern for LocalTime formatting/parsing
     */
    public static final String TIME_FORMAT = "HH:mm";

    /**
     * Pattern for names (required fields): only letters, spaces, hyphens and apostrophes
     * Supports Russian (А-Я, а-я, Ё, ё) and English (A-Z, a-z) characters
     */
    public static final String NAME_REQUIRED = "^[А-Яа-яЁёA-Za-z\\s\\-']+$";

    /**
     * Pattern for names (optional fields): only letters, spaces, hyphens and apostrophes, can be empty
     * Supports Russian (А-Я, а-я, Ё, ё) and English (A-Z, a-z) characters
     */
    public static final String NAME_OPTIONAL = "^[А-Яа-яЁёA-Za-z\\s\\-']*$";
}