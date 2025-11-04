package org.nsu.authorization.core.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.dto.requests.EmailVerifierRequest;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
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
    private EmailVerifierRequest emailVerifierRequest;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private final String TEST_USER_ID = "123";
    private final Long TEST_USER_ID_LONG = 123L;
    private final String TEST_EMAIL = "test@example.com";
    private final String VALID_CODE = "valid-code-123";

    @BeforeEach
    void setUp() {
        // Stubs that are specific to test cases will be added in those test cases
        // to avoid UnnecessaryStubbingException.
    }

    @Test
    void testVerifyEmail_Success() {
        // Arrange
        when(jwt.getClaimAsString("userID")).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL);
        when(emailVerifierRequest.getCode()).thenReturn(VALID_CODE);
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);

        User testUser = new User();
        testUser.setEmailVerified(false);
        when(userRepository.getReferenceById(TEST_USER_ID_LONG)).thenReturn(testUser);

        // Act
        emailVerificationService.verifyEmail(emailVerifierRequest, jwt);

        // Assert
        verify(userRepository, times(1)).getReferenceById(TEST_USER_ID_LONG);
        verify(userRepository, times(1)).save(testUser);
        assertTrue(testUser.isEmailVerified(), "User's email should be marked as verified.");
        verify(verificationCodeCachingService, never()).generateAndCacheCode(anyString());
        verify(emailVerificationSenderService, never()).Send(anyString(), anyString());
    }

    @Test
    void testVerifyEmail_CodeExpiredOrNull_RegeneratesAndSends() {
        // Arrange
        String newGeneratedCode = "new-code-456";
        when(jwt.getClaimAsString("userID")).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL);
        // when(emailVerifierRequest.getCode()).thenReturn(VALID_CODE); // This was unnecessary
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(null); // But it's expired/not in cache
        when(verificationCodeCachingService.generateAndCacheCode(TEST_USER_ID)).thenReturn(newGeneratedCode);

        // Act
        emailVerificationService.verifyEmail(emailVerifierRequest, jwt);

        // Assert
        // Verification does NOT complete, it just regenerates and resends
        verify(verificationCodeCachingService, times(1)).generateAndCacheCode(TEST_USER_ID);
        verify(emailVerificationSenderService, times(1)).Send(TEST_EMAIL, newGeneratedCode);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        // Arrange
        when(jwt.getClaimAsString("userID")).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL);
        String invalidCode = "invalid-code-789";
        String cachedCode = "different-cached-code";
        when(emailVerifierRequest.getCode()).thenReturn(invalidCode);
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(cachedCode);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(emailVerifierRequest, jwt)
        );

        assertTrue(exception.getMessage().contains("invalid verification code"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_MissingUserIdClaim() {
        // Arrange
        when(jwt.getClaimAsString("userID")).thenReturn(null);
        // when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL); // Unnecessary for this test

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(emailVerifierRequest, jwt)
        );

        assertTrue(exception.getMessage().contains("Token is missing 'userID' or 'email' claims"));
    }

    @Test
    void testVerifyEmail_MissingEmailClaim() {
        // Arrange
        when(jwt.getClaimAsString("userID")).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString("email")).thenReturn(null);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(emailVerifierRequest, jwt)
        );

        assertTrue(exception.getMessage().contains("Token is missing 'userID' or 'email' claims"));
    }

    @Test
    void testVerifyEmail_JwtClaimException() {
        // Arrange
        when(jwt.getClaimAsString("userID")).thenThrow(new RuntimeException("Test JWT parsing error"));
        // when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL); // This was unnecessary

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(emailVerifierRequest, jwt)
        );

        assertTrue(exception.getMessage().contains("Failed to extract claims from token"));
    }

    @Test
    void testVerifyEmail_NonNumericUserId() {
        // Arrange
        String nonNumericUserId = "not-a-number";
        when(jwt.getClaimAsString("userID")).thenReturn(nonNumericUserId);
        when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL); // This claim IS read
        // when(emailVerifierRequest.getCode()).thenReturn(VALID_CODE); // This was unnecessary
        when(verificationCodeCachingService.getCode(nonNumericUserId)).thenReturn(VALID_CODE);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(emailVerifierRequest, jwt)
        );

        assertTrue(exception.getMessage().contains("userid must only contain digits"));
    }

    @Test
    void testVerifyEmail_UserNotFound() {
        // Arrange
        when(jwt.getClaimAsString("userID")).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString("email")).thenReturn(TEST_EMAIL);
        when(emailVerifierRequest.getCode()).thenReturn(VALID_CODE);
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);
        when(userRepository.getReferenceById(TEST_USER_ID_LONG)).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(emailVerifierRequest, jwt)
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }
}

