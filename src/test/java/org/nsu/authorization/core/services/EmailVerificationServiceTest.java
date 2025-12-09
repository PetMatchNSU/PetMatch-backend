package org.nsu.authorization.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.dto.requests.EmailVerificationRequest;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.User;

import java.util.Optional;

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

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private final String TEST_USER_ID_STR = "123";
    private final Long TEST_USER_ID_LONG = 123L;
    private final String TEST_EMAIL = "test@example.com";
    private final String VALID_CODE = "123123";

    private User testUser;

    @BeforeEach
    void setUp() {
        // Prepare a common User entity for tests
        testUser = new User();
        testUser.setId(TEST_USER_ID_LONG);
        testUser.setEmail(TEST_EMAIL);
        testUser.setEmailVerified(false);
    }

    @Test
    void testVerifyEmail_Success() {
        // Arrange
        // 1. Mock finding the user by email
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // 2. Mock finding the code in cache using the ID from the user entity
        when(verificationCodeCachingService.getCode(TEST_USER_ID_STR)).thenReturn(VALID_CODE);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(VALID_CODE);

        // Act
        emailVerificationService.verifyEmail(request, TEST_EMAIL);

        // Assert
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(userRepository, times(1)).save(testUser);
        assertTrue(testUser.isEmailVerified());

        // Ensure we didn't regenerate code or send email
        verify(verificationCodeCachingService, never()).generateAndCacheCode(anyString());
        verify(emailVerificationSenderService, never()).send(anyString(), anyString());
    }

    @Test
    void testVerifyEmail_CodeExpiredOrNull_RegeneratesAndSends() {
        // Arrange
        String newGeneratedCode = "123456";

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(verificationCodeCachingService.getCode(TEST_USER_ID_STR)).thenReturn(null); // Code expired
        when(verificationCodeCachingService.generateAndCacheCode(TEST_USER_ID_STR)).thenReturn(newGeneratedCode);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(VALID_CODE);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, TEST_EMAIL)
        );

        // Verify message indicates code was resent
        assertTrue(exception.getMessage().contains("Мы выслали новое письмо"));

        // Assert side effects
        verify(verificationCodeCachingService, times(1)).generateAndCacheCode(TEST_USER_ID_STR);
        verify(emailVerificationSenderService, times(1)).send(TEST_EMAIL, newGeneratedCode);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        // Arrange
        String invalidCode = "999999";

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(verificationCodeCachingService.getCode(TEST_USER_ID_STR)).thenReturn(VALID_CODE);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(invalidCode);

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, TEST_EMAIL)
        );

        assertEquals("Failed to verify email: invalid verification code.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode("any-code");

        // Act & Assert
        EmailVerificationFailException exception = assertThrows(
                EmailVerificationFailException.class,
                () -> emailVerificationService.verifyEmail(request, TEST_EMAIL)
        );

        assertEquals("User not found with email: " + TEST_EMAIL, exception.getMessage());

        // Ensure no cache logic ran
        verify(verificationCodeCachingService, never()).getCode(anyString());
    }
}