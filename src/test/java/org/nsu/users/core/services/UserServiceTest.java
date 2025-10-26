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
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.entity.*;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
    }

    @Test
    void getUserProfile_ShouldReturnUserResponse_WhenUserExists() {
        // Given
        Long userId = 1L;
        Set<Contact> contacts = TestDataFactory.createTestContacts(testUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(contactService.getContactsByUser(testUser)).thenReturn(contacts);
        when(statusCommentService.getLatestCommentByUser(testUser)).thenReturn(Optional.empty());

        // Expected result
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
                        // Время будет конвертировано в московскую зону
                        java.time.OffsetDateTime.now(java.time.ZoneId.of("Europe/Moscow")).with(java.time.LocalTime.of(9, 0)),
                        java.time.OffsetDateTime.now(java.time.ZoneId.of("Europe/Moscow")).with(java.time.LocalTime.of(18, 0))
                )),
                List.of(new UserResponse.ContactInfoDto(
                        "Phone",
                        "+7900123456",
                        true
                ))
        );

        // When
        UserResponse result = userService.getUserProfile(userId);

        // Then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("bondTime.bondTimeStart", "bondTime.bondTimeEnd") // Игнорируем точное время
                .isEqualTo(expectedResponse);
        
        // Проверяем время отдельно
        assertThat(result.getBondTime()).hasSize(1);
        assertThat(result.getBondTime().get(0).getBondTimeStart().toLocalTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.getBondTime().get(0).getBondTimeEnd().toLocalTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void getUserProfile_ShouldThrowPersonNotFoundException_WhenUserNotExists() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(PersonNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void convertLocalTimeToOffsetDateTime_ShouldConvertToMoscowTime() throws Exception {
        // Given
        LocalTime localTime = LocalTime.of(14, 30, 0); // 14:30:00
        
        // Используем рефлексию для доступа к приватному методу
        Method method = UserService.class.getDeclaredMethod("convertLocalTimeToOffsetDateTime", LocalTime.class);
        method.setAccessible(true);

        // When
        OffsetDateTime result = (OffsetDateTime) method.invoke(userService, localTime);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLocalTime()).isEqualTo(localTime);
        
        // Проверяем, что используется московская зона (UTC+3)
        ZoneOffset moscowOffset = ZoneId.of("Europe/Moscow").getRules().getOffset(result.toInstant());
        assertThat(result.getOffset()).isEqualTo(moscowOffset);
    }

    @Test
    void convertLocalTimeToOffsetDateTime_ShouldHandleMidnight() throws Exception {
        // Given
        LocalTime midnight = LocalTime.MIDNIGHT; // 00:00:00
        
        Method method = UserService.class.getDeclaredMethod("convertLocalTimeToOffsetDateTime", LocalTime.class);
        method.setAccessible(true);

        // When
        OffsetDateTime result = (OffsetDateTime) method.invoke(userService, midnight);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLocalTime()).isEqualTo(midnight);
        
        ZoneOffset moscowOffset = ZoneId.of("Europe/Moscow").getRules().getOffset(result.toInstant());
        assertThat(result.getOffset()).isEqualTo(moscowOffset);
    }

    @Test
    void convertLocalTimeToOffsetDateTime_ShouldHandleNoon() throws Exception {
        // Given
        LocalTime noon = LocalTime.NOON; // 12:00:00
        
        Method method = UserService.class.getDeclaredMethod("convertLocalTimeToOffsetDateTime", LocalTime.class);
        method.setAccessible(true);

        // When
        OffsetDateTime result = (OffsetDateTime) method.invoke(userService, noon);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLocalTime()).isEqualTo(noon);
        
        ZoneOffset moscowOffset = ZoneId.of("Europe/Moscow").getRules().getOffset(result.toInstant());
        assertThat(result.getOffset()).isEqualTo(moscowOffset);
    }

    @Test
    void convertLocalTimeToOffsetDateTime_ShouldUseCurrentDateWithMoscowZone() throws Exception {
        // Given
        LocalTime testTime = LocalTime.of(18, 45, 30);
        
        Method method = UserService.class.getDeclaredMethod("convertLocalTimeToOffsetDateTime", LocalTime.class);
        method.setAccessible(true);

        // When
        OffsetDateTime result = (OffsetDateTime) method.invoke(userService, testTime);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLocalTime()).isEqualTo(testTime);
        
        // Проверяем, что дата текущая
        assertThat(result.toLocalDate()).isEqualTo(java.time.LocalDate.now(ZoneId.of("Europe/Moscow")));
        
        // Проверяем московскую зону
        ZoneOffset moscowOffset = ZoneId.of("Europe/Moscow").getRules().getOffset(result.toInstant());
        assertThat(result.getOffset()).isEqualTo(moscowOffset);
    }
}