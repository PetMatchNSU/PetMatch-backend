package org.nsu.authorization.core.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.utils.VerificationCodeGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VerificationCodeCachingService.
 * Note: These tests do not test the Spring Cache behavior itself (which requires integration testing),
 * but rather the logic *within* the service methods.
 */
@ExtendWith(MockitoExtension.class)
class VerificationCodeCachingServiceTest {

    @Mock
    private VerificationCodeGenerator verificationCodeGenerator;

    @InjectMocks
    private VerificationCodeCachingService verificationCodeCachingService;

    @Test
    void testGetCode() {
        // The @Cacheable method's body just returns null.
        // Spring's proxy handles the cache lookup.
        // In a unit test, we expect it to return what the method body returns.
        String code = verificationCodeCachingService.getCode("user-123");
        assertNull(code, "getCode should return null as per its implementation");
    }

    @Test
    void testGenerateAndCacheCode() {
        // Arrange
        String userId = "user-123";
        String expectedCode = "123456";
        when(verificationCodeGenerator.generateVerificationCode()).thenReturn(expectedCode);

        // Act
        String actualCode = verificationCodeCachingService.generateAndCacheCode(userId);

        // Assert
        assertEquals(expectedCode, actualCode, "The generated code should match the expected code.");
        verify(verificationCodeGenerator, times(1)).generateVerificationCode();
    }

    @Test
    void testEvictCode() {
        // The @CacheEvict method's body is empty.
        // We just call it to ensure no exceptions are thrown.
        assertDoesNotThrow(() -> {
            verificationCodeCachingService.clearCode("user-123");
        }, "evictCode should not throw any exceptions.");

        // We can also verify that it doesn't interact with dependencies it shouldn't
        verifyNoInteractions(verificationCodeGenerator);
    }
}
