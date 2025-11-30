package org.nsu.authorization.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nsu.authorization.core.dto.requests.LoginRequest;
import org.nsu.authorization.core.dto.responses.positive.LoginResponse;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.exceptions.handlers.AuthorizationExceptionHandler;
import org.nsu.authorization.core.exceptions.handlers.GlobalExceptionHandler;
import org.nsu.authorization.core.services.LoginService;
import org.nsu.authorization.core.services.PersonDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.config.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.security.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.utils.JWTUtil"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.services.PersonDetailsService")
})
@ContextConfiguration(classes = {LoginController.class, TestSecurityConfig.class,
        AuthorizationExceptionHandler.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class LoginControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginService loginService;

    @MockBean
    private PersonDetailsService personDetailsService;

    @Test
    void login_whenValidCredentials_shouldReturnOkWithTokens() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123");

        LoginResponse mockResponse = new LoginResponse(
                "fake.access.token", "fake.refresh.token", new LoginResponse.UserDto(true)
        );

        when(loginService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("fake.access.token")))
                .andExpect(jsonPath("$.refreshToken", is("fake.refresh.token")));
    }

    @Nested
    @DisplayName("Bug fix tests - Login error responses")
    class LoginErrorResponseTests {

        @Test
        @DisplayName("Bug 2025-11-26: Login with non-existent email should return 400 with 'Неверный email или пароль'")
        void login_whenEmailNotFound_shouldReturn400WithCorrectMessage() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("newuser1@example.com");
            request.setPassword("Password123");

            when(loginService.login(any(LoginRequest.class)))
                    .thenThrow(new PersonNotFoundException("Неверный email или пароль"));

            mockMvc.perform(post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Неверный email или пароль")))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.spanId").exists());
        }

        @Test
        @DisplayName("Bug 2025-11-26: Login with invalid email format should return 400 with 'Неверный email или пароль'")
        void login_whenInvalidEmailFormat_shouldReturn400WithCorrectMessage() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("example.com"); // Invalid email format
            request.setPassword("Password123");

            when(loginService.login(any(LoginRequest.class)))
                    .thenThrow(new PersonNotFoundException("Неверный email или пароль"));

            mockMvc.perform(post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Неверный email или пароль")))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Login with wrong password should return 400 with 'Неверный email или пароль'")
        void login_whenWrongPassword_shouldReturn400WithCorrectMessage() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("WrongPassword123");

            when(loginService.login(any(LoginRequest.class)))
                    .thenThrow(new PersonNotFoundException("Неверный email или пароль"));

            mockMvc.perform(post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Неверный email или пароль")))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }
}
