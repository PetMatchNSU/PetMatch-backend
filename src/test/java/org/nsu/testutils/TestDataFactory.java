package org.nsu.testutils;

import org.nsu.users.entity.*;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
        // Authority has no setters; set fields via reflection
        try {
            Field idField = Authority.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(authority, 1L);

            Field nameField = Authority.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(authority, "USER");

            Field descField = Authority.class.getDeclaredField("description");
            descField.setAccessible(true);
            descField.set(authority, "Regular user");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return authority;
    }

    public static BondTime createTestBondTime(User user) {
        BondTime bondTime = new BondTime();
        bondTime.setId(1L);
        bondTime.setStart(LocalTime.of(9, 0));
        bondTime.setEnd(LocalTime.of(18, 0));
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
        // set isEmailVerified via reflection to be safe
        try {
            Field f = User.class.getDeclaredField("isEmailVerified");
            f.setAccessible(true);
            f.set(user, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        List<BondTime> bondTimes = new ArrayList<>();
        bondTimes.add(createTestBondTime(user));
        user.setBondTimes(bondTimes);

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
        try {
            Field f = User.class.getDeclaredField("isEmailVerified");
            f.setAccessible(true);
            f.set(user, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        user.setBondTimes(new ArrayList<>());
        user.setAuthorities(new HashSet<>());

        return user;
    }

    public static Timestamp createTestTimestampOneHourAgo() {
        return Timestamp.valueOf(LocalDateTime.now().minusHours(1));
    }

    public static Set<Contact> createTestContacts(User user) {
        ContactType contactType = createTestContactType();
        Set<Contact> contacts = new HashSet<>();
        contacts.add(createTestContact(user, contactType));
        return contacts;
    }
}
