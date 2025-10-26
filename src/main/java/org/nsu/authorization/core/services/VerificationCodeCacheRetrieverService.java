package org.nsu.authorization.core.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeCacheRetrieverService {

    @Cacheable(value = "Verification codes", key = "#key")
    public String getCode(String key) {
        return "";
    }

}
