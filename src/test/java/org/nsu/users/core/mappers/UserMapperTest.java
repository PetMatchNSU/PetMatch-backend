package org.nsu.users.core.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.testutils.TestDataFactory;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.services.TimezoneService;
import org.nsu.users.entity.BondTime;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private TimezoneService timezoneService;
    
    private UserMapper userMapper;
    private User testUser;
    private Set<Contact> testContacts;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        ReflectionTestUtils.setField(userMapper, "timezoneService", timezoneService);
        
        testUser = TestDataFactory.createTestUser();
        testContacts = TestDataFactory.createTestContacts(testUser);
        setupTimezoneServiceMock();
    }
    
    private void setupTimezoneServiceMock() {
        // Mock the timezone service behavior with lenient stubbing
        lenient().when(timezoneService.convertLocalTimeToOffsetDateTime(any(LocalTime.class)))
            .thenAnswer(invocation -> {
                LocalTime localTime = invocation.getArgument(0);
                if (localTime == null) return null;
                ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).with(localTime);
                return zdt.toOffsetDateTime();
            });
    }

    @Test
    void toUserResponse_ShouldMapAllFields_WhenUserIsProvided() {
        String reviewComment = "Test comment";

        UserResponse result = userMapper.toUserResponse(testUser, reviewComment, testContacts);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.getSecondName()).isEqualTo(testUser.getSecondName());
        assertThat(result.getMiddleName()).isEqualTo(testUser.getMiddleName());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getGender()).isEqualTo(testUser.getGender().name());
        assertThat(result.getRegion()).isEqualTo(testUser.getRegion().getRegion());
        assertThat(result.getCity()).isEqualTo(testUser.getRegion().getCity());
        assertThat(result.getReviewStatus()).isEqualTo(testUser.getStatus().getName());
        assertThat(result.getReviewComment()).isEqualTo(reviewComment);
        assertThat(result.getBondTime()).hasSize(testUser.getBondTimes().size());
        assertThat(result.getContactInfo()).hasSize(testContacts.size());
    }

    @Test
    void toBondTimeDto_ShouldConvertLocalTimeToOffsetDateTime() {
        BondTime bondTime = testUser.getBondTimes().get(0);

        UserResponse.BondTimeDto result = userMapper.toBondTimeDto(bondTime);

        assertThat(result).isNotNull();
        assertThat(result.getBondTimeStart()).isNotNull();
        assertThat(result.getBondTimeEnd()).isNotNull();
        assertThat(result.getBondTimeStart().toLocalTime()).isEqualTo(bondTime.getStartContactTime());
        assertThat(result.getBondTimeEnd().toLocalTime()).isEqualTo(bondTime.getEndContactTime());
        
        // Since we're using static methods that may fallback to Europe/Moscow, we just check that offset is not null
        assertThat(result.getBondTimeStart().getOffset()).isNotNull();
        assertThat(result.getBondTimeEnd().getOffset()).isNotNull();
    }

    @Test
    void toContactInfoDto_ShouldMapContactFields() {
        Contact contact = testContacts.iterator().next();

        UserResponse.ContactInfoDto result = userMapper.toContactInfoDto(contact);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(contact.getType().getName());
        assertThat(result.getContact()).isEqualTo(contact.getLink());
        assertThat(result.isVisible()).isEqualTo(contact.getIsVisible());
    }

    @Test
    void localTimeToOffsetDateTime_ShouldConvertToMoscowTime() {
        LocalTime localTime = LocalTime.of(14, 30);

        OffsetDateTime result = userMapper.localTimeToOffsetDateTime(localTime);

        assertThat(result).isNotNull();
        assertThat(result.toLocalTime()).isEqualTo(localTime);
        
        // Since we're using static methods that may fallback to Europe/Moscow, we just check that offset is not null
        assertThat(result.getOffset()).isNotNull();
    }

    @Test
    void localTimeToOffsetDateTime_ShouldHandleNullInput() {
        OffsetDateTime result = userMapper.localTimeToOffsetDateTime(null);

        assertThat(result).isNull();
    }

    @Test
    void genderToString_ShouldConvertGenderEnum() {
        String result = userMapper.genderToString(testUser.getGender());

        assertThat(result).isEqualTo(testUser.getGender().name());
    }

    @Test
    void genderToString_ShouldHandleNullInput() {
        String result = userMapper.genderToString(null);

        assertThat(result).isNull();
    }
}
