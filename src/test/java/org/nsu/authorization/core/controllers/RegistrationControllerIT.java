package org.nsu.authorization.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nsu.authorization.core.dto.requests.registrationRequest.BondTime;
import org.nsu.authorization.core.dto.requests.registrationRequest.ContactInfo;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.dto.requests.registrationRequest.Type;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.exceptions.authorization.RegionNotFoundException;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.nsu.authorization.core.exceptions.handlers.AuthorizationExceptionHandler;
import org.nsu.authorization.core.exceptions.handlers.GlobalExceptionHandler;
import org.nsu.authorization.core.services.PersonDetailsService;
import org.nsu.authorization.core.services.RegistrationService;
import org.nsu.users.entity.Gender;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegistrationController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.config.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.security.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.utils.JWTUtil"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.services.PersonDetailsService")
})
@ContextConfiguration(classes = {RegistrationController.class, TestSecurityConfig.class,
        AuthorizationExceptionHandler.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class RegistrationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private PersonDetailsService personDetailsService;

    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setFirstName("John");
        validRequest.setSecondName("Doe");
        validRequest.setLastName("Smith");
        validRequest.setGender(Gender.M);
        validRequest.setRegion("Novosibirsk");
        validRequest.setCity("Novosibirsk");

        BondTime dummyBondTime = new BondTime();
        dummyBondTime.setBondTimeStart(LocalTime.parse("10:00"));
        dummyBondTime.setBondTimeEnd(LocalTime.parse("12:00"));

        ContactInfo dummyContact = new ContactInfo();
        dummyContact.setType(Type.TELEGRAM);
        dummyContact.setContact("@testuser");
        dummyContact.setVisible(true);

        validRequest.setBondTime(List.of(dummyBondTime));
        validRequest.setContactInfo(List.of(dummyContact));
    }

    @Test
    void register_whenValidRequest_shouldReturnOKAndTokens() throws Exception {
        RegistrationResponse mockResponse = new RegistrationResponse(
                "fake.access.token", "fake.refresh.token", new RegistrationResponse.UserDto()
        );

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

    @Nested
    @DisplayName("Bug fix tests - Error response format")
    class ErrorResponseFormatTests {

        @Test
        @DisplayName("Bug 2025-11-19: Response should contain 'message' field, not 'error' when email already exists")
        void register_whenEmailAlreadyExists_shouldReturnMessageFieldNotError() throws Exception {
            when(registrationService.register(any(RegistrationRequest.class)))
                    .thenThrow(new UserAlreadyExistsException("A user with this email already exists."));

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", is("A user with this email already exists.")))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.spanId").exists());
        }

        @Test
        @DisplayName("Bug 2025-11-25: Should return 400 with 'message' field when region not found")
        void register_whenRegionNotFound_shouldReturn400WithMessageField() throws Exception {
            when(registrationService.register(any(RegistrationRequest.class)))
                    .thenThrow(new RegionNotFoundException("Новосибирская область, City"));

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Region not found")))
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Validation errors should return 'message' field, not 'error'")
        void register_whenValidationFails_shouldReturnMessageFieldNotError() throws Exception {
            validRequest.setEmail(""); // Invalid: empty email

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: Invalid email format should return 400")
        void register_whenInvalidEmailFormat_shouldReturn400() throws Exception {
            validRequest.setEmail("example.com"); // Invalid email format

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: Missing email field should return 400")
        void register_whenEmailMissing_shouldReturn400() throws Exception {
            validRequest.setEmail(null);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Bug fix tests - Name validation")
    class NameValidationTests {

        @Test
        @DisplayName("Bug 2025-11-25: firstName with 45 characters should be accepted (max 64)")
        void register_whenFirstName45Characters_shouldBeAccepted() throws Exception {
            validRequest.setFirstName("A".repeat(45)); // 45 characters, within 64 limit

            RegistrationResponse mockResponse = new RegistrationResponse(
                    "fake.access.token", "fake.refresh.token", new RegistrationResponse.UserDto()
            );
            when(registrationService.register(any(RegistrationRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Bug 2025-11-25: firstName with 65 characters should return 400, not 409")
        void register_whenFirstName65Characters_shouldReturn400NotConflict() throws Exception {
            validRequest.setFirstName("A".repeat(65)); // 65 characters, exceeds 64 limit

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("First name must not exceed 64 characters")))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: firstName with digits should return 400")
        void register_whenFirstNameContainsDigits_shouldReturn400() throws Exception {
            validRequest.setFirstName("Тестовик1233");

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: secondName with digits should return 400")
        void register_whenSecondNameContainsDigits_shouldReturn400() throws Exception {
            validRequest.setSecondName("Тестович123");

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: lastName with digits should return 400")
        void register_whenLastNameContainsDigits_shouldReturn400() throws Exception {
            validRequest.setLastName("Тестер123");

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: firstName with special characters should return 400")
        void register_whenFirstNameContainsSpecialChars_shouldReturn400() throws Exception {
            validRequest.setFirstName("Иван!@#");

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Names with hyphens and apostrophes should be accepted")
        void register_whenNameContainsHyphensAndApostrophes_shouldBeAccepted() throws Exception {
            validRequest.setFirstName("Анна-Мария");
            validRequest.setSecondName("О'Коннор");

            RegistrationResponse mockResponse = new RegistrationResponse(
                    "fake.access.token", "fake.refresh.token", new RegistrationResponse.UserDto()
            );
            when(registrationService.register(any(RegistrationRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Bug fix tests - BondTime validation")
    class BondTimeValidationTests {

        @Test
        @DisplayName("Bug 2025-11-19: More than 4 bond time intervals should return 400")
        void register_whenMoreThan4BondTimeIntervals_shouldReturn400() throws Exception {
            List<BondTime> bondTimes = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                BondTime bt = new BondTime();
                bt.setBondTimeStart(LocalTime.of(8 + i * 2, 0));
                bt.setBondTimeEnd(LocalTime.of(10 + i * 2, 0));
                bondTimes.add(bt);
            }
            validRequest.setBondTime(bondTimes);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: Reversed bond time (start > end) should return 400")
        void register_whenBondTimeReversed_shouldReturn400() throws Exception {
            BondTime reversedBondTime = new BondTime();
            reversedBondTime.setBondTimeStart(LocalTime.of(18, 0));
            reversedBondTime.setBondTimeEnd(LocalTime.of(16, 0));
            validRequest.setBondTime(List.of(reversedBondTime));

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Bug 2025-11-19: Overlapping bond time intervals should return 400")
        void register_whenBondTimeIntervalsOverlap_shouldReturn400() throws Exception {
            BondTime bt1 = new BondTime();
            bt1.setBondTimeStart(LocalTime.of(10, 0));
            bt1.setBondTimeEnd(LocalTime.of(14, 0));

            BondTime bt2 = new BondTime();
            bt2.setBondTimeStart(LocalTime.of(12, 0));
            bt2.setBondTimeEnd(LocalTime.of(16, 0));

            validRequest.setBondTime(List.of(bt1, bt2));

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("4 non-overlapping bond time intervals should be accepted")
        void register_when4ValidBondTimeIntervals_shouldBeAccepted() throws Exception {
            List<BondTime> bondTimes = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                BondTime bt = new BondTime();
                bt.setBondTimeStart(LocalTime.of(8 + i * 3, 0));
                bt.setBondTimeEnd(LocalTime.of(10 + i * 3, 0));
                bondTimes.add(bt);
            }
            validRequest.setBondTime(bondTimes);

            RegistrationResponse mockResponse = new RegistrationResponse(
                    "fake.access.token", "fake.refresh.token", new RegistrationResponse.UserDto()
            );
            when(registrationService.register(any(RegistrationRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Bug fix tests - ContactInfo validation")
    class ContactInfoValidationTests {

        @Test
        @DisplayName("Bug 2025-11-19: More than 10 contacts should return 400")
        void register_whenMoreThan10Contacts_shouldReturn400() throws Exception {
            List<ContactInfo> contacts = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                ContactInfo contact = new ContactInfo();
                contact.setType(Type.VK);
                contact.setContact("https://vk.com/user" + (i + 1));
                contact.setVisible(true);
                contacts.add(contact);
            }
            validRequest.setContactInfo(contacts);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("10 contacts should be accepted")
        void register_when10Contacts_shouldBeAccepted() throws Exception {
            List<ContactInfo> contacts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                ContactInfo contact = new ContactInfo();
                contact.setType(Type.VK);
                contact.setContact("https://vk.com/user" + (i + 1));
                contact.setVisible(true);
                contacts.add(contact);
            }
            validRequest.setContactInfo(contacts);

            RegistrationResponse mockResponse = new RegistrationResponse(
                    "fake.access.token", "fake.refresh.token", new RegistrationResponse.UserDto()
            );
            when(registrationService.register(any(RegistrationRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }
}