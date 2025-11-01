package org.nsu.authorization.core.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        VerificationCodeGenerator.class,
        VerificationCodeGeneratorIntegrationTest.TestCachingConfig.class
})
@EnableCaching
class VerificationCodeGeneratorIntegrationTest {

    @Autowired
    private VerificationCodeGenerator verificationCodeGenerator;

    @Autowired
    private CacheManager cacheManager;

    private static final String CACHE_NAME = "Verification codes";
    // The key must match the one used in RegistrationService:
    // "registrationService:user:1:email:code"
    private static final String TEST_KEY = "test:user:1:email:code";

    @AfterEach
    void tearDown() {
        cacheManager.getCache(CACHE_NAME).clear();
    }

    @Test
    void testGenerateVerificationCodeAndCacheIt() {
        assertNotNull(cacheManager, "CacheManager should be injected successfully.");

        String code1 = verificationCodeGenerator.generateVerificationCodeAndCacheIt(TEST_KEY);

        String code2 = verificationCodeGenerator.generateVerificationCodeAndCacheIt(TEST_KEY);

        assertNotNull(code1);
        assertNotEquals("", code1);
        assertEquals(code1, code2, "The second call should return the cached value from the first call.");

        // OPTIONAL: Verify the code is actually in the cache (requires reflection
        // access to ConcurrentMapCacheManager)
        // This confirms the cache operation was performed correctly.
        Cache cache = cacheManager.getCache("Verification codes");
        String cachedCode = cache.get(TEST_KEY, String.class);
        assertEquals(code1, cachedCode, "The code retrieved directly from the cache should match the generated code.");
    }

    /**
     * Test Configuration to provide a simple in-memory CacheManager,
     * satisfying the @Cacheable dependency without requiring a live Redis
     * connection.
     */
    @TestConfiguration
    @EnableCaching
    static class TestCachingConfig {
        @Bean
        public CacheManager cacheManager() {
            // Must define the cache name used in the service: "Verification codes"
            // The ConcurrentMapCacheManager creates a simple, in-memory cache.
            return new ConcurrentMapCacheManager("Verification codes");
        }
    }
}