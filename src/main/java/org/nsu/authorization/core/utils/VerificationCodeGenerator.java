package org.nsu.authorization.core.utils;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

    @Cacheable(value = "Verification codes", key = "#email")
    public String generateVerificationCodeAndCacheIt(String email) {
        return UUID.randomUUID().toString();
    }
}
