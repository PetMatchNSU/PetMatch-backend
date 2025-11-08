package org.nsu.authorization.core.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.nsu.authorization.core.exceptions.authorization.VerificationCodeGenerationFailException;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

    public String generateVerificationCode() {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new VerificationCodeGenerationFailException("Failed to generate code for email verification", e);
        }
        int myInt = sr.nextInt(900000) + 100000;
        return String.valueOf(myInt);
    }
}