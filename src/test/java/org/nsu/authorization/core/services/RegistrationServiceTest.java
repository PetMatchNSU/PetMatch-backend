package org.nsu.authorization.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private EmailVerificationSenderService emailVerificationSenderService;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private VerificationCodeCachingService verificationCodeCachingService;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationRequest registrationRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFirstName("Test");
        registrationRequest.setSecondName("User");
        registrationRequest.setLastName("McTest");
        registrationRequest.setGender(Gender.M);
        registrationRequest.setRegion("Test Region");

        mockUser = mock(User.class);
    }

    @Test
    void testRegister_Success() {
        when(mockUser.getId()).thenReturn(1L);

        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        when(userService.AddNewUser(registrationRequest)).thenReturn(mockUser);

        String testCode = "123456";
        String expectedCacheKey = "1";
        when(verificationCodeCachingService.generateAndCacheCode(expectedCacheKey)).thenReturn(testCode);

        String accessToken = "fake-access-token";
        String refreshToken = "fake-refresh-token";
        when(jwtUtil.generateAccessToken(any(UsernamePasswordAuthenticationToken.class))).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(any(UsernamePasswordAuthenticationToken.class))).thenReturn(refreshToken);

        RegistrationResponse response = registrationService.register(registrationRequest);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertFalse(response.getUser().isEmailVerified());

        verify(userService).existsByEmail("test@example.com");
        verify(userService).AddNewUser(registrationRequest);
        verify(verificationCodeCachingService).generateAndCacheCode(expectedCacheKey);
        verify(emailVerificationSenderService).Send("test@example.com", testCode);
        verify(jwtUtil).generateAccessToken(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateRefreshToken(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            registrationService.register(registrationRequest);
        });

        verify(userService, never()).AddNewUser(any());
        verify(verificationCodeCachingService, never()).generateAndCacheCode(anyString());
        verify(emailVerificationSenderService, never()).Send(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(any());
    }
}