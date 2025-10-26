package org.nsu.testutils;

import org.nsu.admin.entity.StatusComment;
import org.nsu.authorization.core.dto.responses.positive.LoginResponse;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.entity.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestDataFactory {

    public static Status createTestStatus() {
        Status status = new Status();
        status.setId(1L);
        status.setName("Active");
        return status;
    }

    public static Region createTestRegion() {
        Region region = new Region();
        region.setId(1L);
        region.setRegion("Moscow Region");
        region.setCity("Moscow");
        return region;
    }

    public static Authority createTestAuthority() {
        Authority authority = new Authority();
        authority.setId(1L);
        authority.setName("USER");
        authority.setDescription("Regular user");
        return authority;
    }

    public static BondTime createTestBondTime(User user) {
        BondTime bondTime = new BondTime();
        bondTime.setId(1L);
        bondTime.setStartContactTime(LocalTime.of(9, 0));
        bondTime.setEndContactTime(LocalTime.of(18, 0));
        bondTime.setUser(user);
        return bondTime;
    }

    public static ContactType createTestContactType() {
        ContactType contactType = new ContactType();
        contactType.setId(1L);
        contactType.setName("Phone");
        return contactType;
    }

    public static Contact createTestContact(User user, ContactType contactType) {
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setLink("+7900123456");
        contact.setIsVisible(true);
        contact.setType(contactType);
        contact.setUser(user);
        return contact;
    }

    public static User createTestUser() {
        Status status = createTestStatus();
        Region region = createTestRegion();
        
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Ivan");
        user.setSecondName("Petrov");
        user.setLastName("Sidorovich");
        user.setStatus(status);
        user.setGender(Gender.M);
        user.setRegion(region);
        user.setEmailVerified(true);
        
        // Добавляем BondTime
        List<BondTime> bondTimes = new ArrayList<>();
        bondTimes.add(createTestBondTime(user));
        user.setBondTimes(bondTimes);
        
        // Добавляем Authority
        Set<Authority> authorities = new HashSet<>();
        authorities.add(createTestAuthority());
        user.setAuthorities(authorities);
        
        return user;
    }

    public static User createTestUserWithoutBondTimeAndAuthorities() {
        Status status = createTestStatus();
        Region region = createTestRegion();
        
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Ivan");
        user.setSecondName("Petrov");
        user.setLastName("Sidorovich");
        user.setStatus(status);
        user.setGender(Gender.M);
        user.setRegion(region);
        user.setEmailVerified(true);
        user.setBondTimes(new ArrayList<>());
        user.setAuthorities(new HashSet<>());
        
        return user;
    }

    public static StatusComment createTestStatusComment(User user) {
        StatusComment comment = new StatusComment();
        comment.setId(1L);
        comment.setUser(user);
        comment.setComment("Test comment");
        comment.setDate(Timestamp.valueOf(LocalDateTime.now().minusHours(1)));
        comment.setStatus(createTestStatus());
        return comment;
    }

    public static UserResponse createTestUserResponse() {
        return new UserResponse(
                "Ivan",
                "Petrov",
                "Sidorovich",
                "test@example.com",
                "M",
                "Moscow Region",
                "Moscow",
                "OK",
                null,
                List.of(),
                List.of()
        );
    }

    public static UserResponse createCompleteUserResponse() {
        return new UserResponse(
                "Test",
                "User",
                "Testovich",
                "complete@example.com",
                "F",
                "Test Region",
                "Test City",
                "ON_CHECKING",
                "Under review",
                List.of(new UserResponse.BondTimeDto(
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plusHours(2)
                )),
                List.of(new UserResponse.ContactInfoDto(
                        "Phone",
                        "+7900123456",
                        true
                ))
        );
    }

    public static LoginResponse createTestLoginResponse() {
        return new LoginResponse(
                "mock.access.token",
                "mock.refresh.token",
                new LoginResponse.UserDto(true)
        );
    }

    public static LoginResponse createCompleteLoginResponse() {
        return new LoginResponse(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.access",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.refresh",
                new LoginResponse.UserDto(true)
        );
    }

    public static Set<Contact> createTestContacts(User user) {
        ContactType contactType = createTestContactType();
        Set<Contact> contacts = new HashSet<>();
        contacts.add(createTestContact(user, contactType));
        return contacts;
    }
}