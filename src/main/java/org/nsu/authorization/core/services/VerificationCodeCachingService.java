package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.utils.CacheUtil;
import org.nsu.authorization.core.utils.VerificationCodeGenerator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeCachingService {
    private final VerificationCodeGenerator verificationCodeGenerator;

    public static final String TEMP_CODE_KEY_FIRST_PART = "user:";
    public static final String TEMP_CODE_KEY_SECOND_PART = ":email:code";

    /**
     * Retrieves the code from the cache based on userId.
     *
     * @param userId The cache key.
     * @return The cached code (String) or null if not found.
     */
    @Cacheable(value = CacheUtil.VERIFICATION_CODE_CACHE_NAME, key = "T(org.nsu.authorization.core.services.VerificationCodeCachingService).TEMP_CODE_KEY_FIRST_PART + #userId + T(org.nsu.authorization.core.services.VerificationCodeCachingService).TEMP_CODE_KEY_SECOND_PART", unless = "#result == null")
    public String getCode(String userId) {
        return null;
    }

    /**
     * Generates a new code, and puts the result in the cache.
     *
     * @param userId   The cache key.
     * @return The newly generated code, which @CachePut then stores.
     */
    @CachePut(value = CacheUtil.VERIFICATION_CODE_CACHE_NAME, key = "T(org.nsu.authorization.core.services.VerificationCodeCachingService).TEMP_CODE_KEY_FIRST_PART + #userId + T(org.nsu.authorization.core.services.VerificationCodeCachingService).TEMP_CODE_KEY_SECOND_PART")
    public String generateAndCacheCode(String userId) {
        return verificationCodeGenerator.generateVerificationCode();
    }

    /**
     * Removes the code from the cache after successful verification.
     *
     * @param userId The cache key to evict.
     */
    @CacheEvict(value = CacheUtil.VERIFICATION_CODE_CACHE_NAME, key = "T(org.nsu.authorization.core.services.VerificationCodeCachingService).TEMP_CODE_KEY_FIRST_PART + #userId + T(org.nsu.authorization.core.services.VerificationCodeCachingService).TEMP_CODE_KEY_SECOND_PART")
    public void clearCode(String userId) {
    }
}
