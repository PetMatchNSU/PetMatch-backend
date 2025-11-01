package org.nsu.authorization.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.nsu.authorization.core.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.nsu.authorization.core.exceptions.handlers.GlobalExceptionHandler;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.nsu.authorization.core.dto.requests.registrationRequest.BondTime;
import org.nsu.authorization.core.dto.requests.registrationRequest.ContactInfo;

@WebMvcTest(controllers = RegistrationController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.config.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.security.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.utils.JWTUtil"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.services.PersonDetailsService")
})
@ContextConfiguration(classes = { RegistrationController.class, TestSecurityConfig.class,
        GlobalExceptionHandler.class })
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setFirstName("John");
        validRequest.setSecondName("Doe");
        validRequest.setLastName("Smith");
        validRequest.setGender("M");
        validRequest.setRegion("Novosibirsk");
        validRequest.setCity("Novosibirsk");

        // --- FIX: Create and add valid DTOs for the lists ---

        // 1. Create a valid BondTime object
        // This satisfies the @NotBlank fields in BondTime.java
        BondTime dummyBondTime = new BondTime();
        dummyBondTime.setBondTimeStart("10:00");
        dummyBondTime.setBondTimeEnd("12:00");

        // 2. Create a valid ContactInfo object
        // This satisfies the @NotBlank and @NotNull fields in ContactInfo.java
        ContactInfo dummyContact = new ContactInfo();
        dummyContact.setType("TELEGRAM");
        dummyContact.setContact("@testuser");
        dummyContact.setVisible(true);

        // 3. Set the lists with these valid objects
        // This satisfies the @Size(min = 1) and @Valid in RegistrationRequest.java
        validRequest.setBondTime(List.of(dummyBondTime));
        validRequest.setContactInfo(List.of(dummyContact));
    }

    @Test
    void register_whenValidRequest_shouldReturnSuccess() throws Exception {
        RegistrationResponse mockResponse = new RegistrationResponse(
                "fake.access.token",
                "fake.refresh.token",
                new RegistrationResponse.UserDto(false));

        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("fake.access.token")));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturnConflict() throws Exception {
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already taken"));

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_whenDatabaseIsDown_shouldReturnInternalServerError() throws Exception {
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenThrow(new DataAccessResourceFailureException("Simulating DB connection failed"));

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }
}