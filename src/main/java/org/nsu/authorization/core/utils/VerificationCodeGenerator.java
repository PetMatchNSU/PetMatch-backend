package org.nsu.authorization.core.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

    public String generateVerificationCode() {
        return UUID.randomUUID().toString();
    }
}