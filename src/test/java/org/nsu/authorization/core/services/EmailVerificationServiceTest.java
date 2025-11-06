package org.nsu.authorization.core.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.dto.requests.EmailVerificationRequest;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
import org.nsu.authorization.core.utils.JwtClaimKey;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationSenderService emailVerificationSenderService;

    @Mock
    private VerificationCodeCachingService verificationCodeCachingService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private final String TEST_USER_ID = "123";
    private final Long TEST_USER_ID_LONG = 123L;
    private final String TEST_EMAIL = "test@example.com";
    private final String VALID_CODE = "123123";

    @Test
    void testVerifyEmail_Success() {
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(TEST_EMAIL);
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);

        User testUser = new User();
        testUser.setEmailVerified(false);
        when(userRepository.getReferenceById(TEST_USER_ID_LONG)).thenReturn(testUser);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(VALID_CODE);

        // Act
        emailVerificationService.verifyEmail(request, jwt);

        // Assert
        verify(userRepository, times(1)).getReferenceById(TEST_USER_ID_LONG);
        verify(userRepository, times(1)).save(testUser);
        assertTrue(testUser.isEmailVerified());
        verify(verificationCodeCachingService, never()).generateAndCacheCode(anyString());
        verify(emailVerificationSenderService, never()).send(anyString(), anyString());
    }

    @Test
    void testVerifyEmail_CodeExpiredOrNull_RegeneratesAndSends() {
        // Arrange
        String newGeneratedCode = "123456";
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(TEST_EMAIL);
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(null);
        when(verificationCodeCachingService.generateAndCacheCode(TEST_USER_ID)).thenReturn(newGeneratedCode);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(VALID_CODE);

        // Act
        emailVerificationService.verifyEmail(request, jwt);

        // Assert
        verify(verificationCodeCachingService, times(1)).generateAndCacheCode(TEST_USER_ID);
        verify(emailVerificationSenderService, times(1)).send(TEST_EMAIL, newGeneratedCode);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(TEST_EMAIL);

        String invalidCode = "287364";
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(invalidCode);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, jwt)
        );

        assertEquals("Failed to verify email: invalid verification code.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_MissingUserIdClaim() {
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(null);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(TEST_EMAIL); // Still need this

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode("any-code");

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, jwt)
        );

        assertEquals("Failed to verify email: Token is missing user ID or email claims.", exception.getMessage());
    }

    @Test
    void testVerifyEmail_MissingEmailClaim() {
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(null);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode("any-code");

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, jwt)
        );

        assertEquals("Failed to verify email: Token is missing user ID or email claims.", exception.getMessage());
    }

    @Test
    void testVerifyEmail_JwtClaimException() {
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenThrow(new RuntimeException("Test JWT parsing error"));

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode("any-code");

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, jwt)
        );

        assertTrue(exception.getMessage().contains("Failed to extract claims from token: Test JWT parsing error"));
    }

    @Test
    void testVerifyEmail_NonNumericUserId() {
        // Arrange
        String nonNumericUserId = "not-a-number";
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(nonNumericUserId);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(TEST_EMAIL);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode("any-code");

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, jwt)
        );

        assertEquals("Failed to verify email: user id must only contain digits.", exception.getMessage());
    }

    @Test
    void testVerifyEmail_UserNotFound() {
        when(jwt.getClaimAsString(JwtClaimKey.USER_ID)).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString(JwtClaimKey.USER_EMAIL)).thenReturn(TEST_EMAIL);
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);
        when(userRepository.getReferenceById(TEST_USER_ID_LONG)).thenThrow(new EntityNotFoundException("User not found"));

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(VALID_CODE);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, jwt)
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }
}