package org.nsu.users.core.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.nsu.testutils.TestDataFactory;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.entity.BondTime;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private Set<Contact> testContacts;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        testUser = TestDataFactory.createTestUser();
        testContacts = TestDataFactory.createTestContacts(testUser);
    }

    @Test
    void toUserResponse_ShouldMapAllFields_WhenUserIsProvided() {
        String reviewComment = "Test comment";

        UserResponse result = userMapper.toUserResponse(testUser, reviewComment, testContacts);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.getSecondName()).isEqualTo(testUser.getSecondName());
        assertThat(result.getLastName()).isEqualTo(testUser.getLastName());
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
        
        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        assertThat(result.getBondTimeStart().getOffset())
            .isEqualTo(moscowZone.getRules().getOffset(result.getBondTimeStart().toInstant()));
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
        
        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        assertThat(result.getOffset())
            .isEqualTo(moscowZone.getRules().getOffset(result.toInstant()));
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