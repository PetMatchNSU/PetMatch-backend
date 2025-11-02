package org.nsu.users.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.admin.services.StatusCommentService;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.testutils.TestDataFactory;
import org.nsu.users.core.mappers.UserMapper;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.entity.*;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactService contactService;

    @Mock
    private StatusCommentService statusCommentService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
    }

    @Test
    void getUserProfile_ShouldReturnUserResponse_WhenUserExists() {
        Long userId = 1L;
        Set<Contact> contacts = TestDataFactory.createTestContacts(testUser);

        UserResponse expectedResponse = new UserResponse(
                "Ivan",
                "Petrov",
                "Sidorovich",
                "test@example.com",
                "M",
                "Moscow Region",
                "Moscow",
                "Active",
                null,
                List.of(new UserResponse.BondTimeDto(
                        java.time.OffsetDateTime.now(java.time.ZoneId.of("Europe/Moscow")).with(java.time.LocalTime.of(9, 0)),
                        java.time.OffsetDateTime.now(java.time.ZoneId.of("Europe/Moscow")).with(java.time.LocalTime.of(18, 0))
                )),
                List.of(new UserResponse.ContactInfoDto(
                        "Phone",
                        "+7900123456",
                        true
                ))
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(contactService.getContactsByUser(testUser)).thenReturn(contacts);
        when(statusCommentService.getLatestCommentByUser(testUser)).thenReturn(Optional.empty());
        when(userMapper.toUserResponse(testUser, null, contacts)).thenReturn(expectedResponse);

        UserResponse result = userService.getUserProfile();

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getUserProfile_ShouldThrowPersonNotFoundException_WhenUserNotExists() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile())
                .isInstanceOf(PersonNotFoundException.class)
                .hasMessage("User not found");
    }
}
