package org.nsu.admin.dto;

public enum LockType {
    USER("app.lock.user.key-prefix", "app.lock.user.ttl-minutes"),
    CARD("app.lock.card.key-prefix", "app.lock.card.ttl-minutes");

    private final String keyPrefixConfigKey;
    private final String ttlMinutesConfigKey;

    LockType(String keyPrefixConfigKey, String ttlMinutesConfigKey) {
        this.keyPrefixConfigKey = keyPrefixConfigKey;
        this.ttlMinutesConfigKey = ttlMinutesConfigKey;
    }

    public String getKeyPrefixConfigKey() {
        return keyPrefixConfigKey;
    }

    public String getTtlMinutesConfigKey() {
        return ttlMinutesConfigKey;
    }
}
