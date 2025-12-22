package org.nsu.testutils;

import org.nsu.admin.entity.StatusComment;
import org.nsu.animal.dto.enums.Gender;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.animal.entity.*;
import org.nsu.authorization.core.dto.responses.positive.LoginResponse;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.entity.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
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
        status.setName("OK");
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
        user.setMiddleName("Sidorovich");
        user.setStatus(status);
        user.setGender(org.nsu.users.entity.Gender.M);
        user.setRegion(region);
        user.setEmailVerified(true);
        
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
        user.setMiddleName("Sidorovich");
        user.setStatus(status);
        user.setGender(org.nsu.users.entity.Gender.M);
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

    public static List<BondTime> createTestBondTimes(User user) {
        List<BondTime> bondTimes = new ArrayList<>();
        
        BondTime bondTime1 = new BondTime();
        bondTime1.setId(1L);
        bondTime1.setStartContactTime(LocalTime.of(10, 0));
        bondTime1.setEndContactTime(LocalTime.of(12, 0));
        bondTime1.setUser(user);
        bondTimes.add(bondTime1);
        
        BondTime bondTime2 = new BondTime();
        bondTime2.setId(2L);
        bondTime2.setStartContactTime(LocalTime.of(16, 0));
        bondTime2.setEndContactTime(LocalTime.of(18, 0));
        bondTime2.setUser(user);
        bondTimes.add(bondTime2);
        
        return bondTimes;
    }

    public static List<Contact> createTestContactsList(User user) {
        List<Contact> contacts = new ArrayList<>();
        
        ContactType vkType = new ContactType();
        vkType.setId(1L);
        vkType.setName("VK");
        
        Contact visibleContact = new Contact();
        visibleContact.setId(1L);
        visibleContact.setLink("https://vk.com/t.test");
        visibleContact.setIsVisible(true);
        visibleContact.setType(vkType);
        visibleContact.setUser(user);
        contacts.add(visibleContact);
        
        ContactType phoneType = new ContactType();
        phoneType.setId(2L);
        phoneType.setName("PHONE");
        
        Contact hiddenContact = new Contact();
        hiddenContact.setId(2L);
        hiddenContact.setLink("+79001234567");
        hiddenContact.setIsVisible(false);
        hiddenContact.setType(phoneType);
        hiddenContact.setUser(user);
        contacts.add(hiddenContact);
        
        return contacts;
    }

    public static Animal createTestAnimal() {
        Animal animal = new Animal();
        animal.setId(1L);
        animal.setName("кошка");
        return animal;
    }

    public static PlacementGoal createTestPlacementGoal() {
        PlacementGoal goal = new PlacementGoal();
        goal.setId(1L);
        goal.setGoal("продажа");
        return goal;
    }

    public static AnimalCardStatus createTestAnimalCardStatus() {
        AnimalCardStatus status = new AnimalCardStatus();
        status.setId(1L);
        status.setName("ON_CHECKING");
        return status;
    }

    public static AnimalCard createTestAnimalCard(User user) {
        Animal animal = createTestAnimal();
        PlacementGoal goal = createTestPlacementGoal();
        AnimalCardStatus status = createTestAnimalCardStatus();

        AnimalCard animalCard = new AnimalCard();
        animalCard.setId(1L);
        animalCard.setCardAuthor(user);
        animalCard.setName("Барсик");
        animalCard.setAnimal(animal);
        animalCard.setHasBreed(true);
        animalCard.setBreed("Британская короткошерстная");
        animalCard.setGender(AnimalGender.M);
        animalCard.setBirthdate(LocalDate.of(2023, 5, 15));
        animalCard.setWeight(new BigDecimal("4.5"));
        animalCard.setColor("Серый с белыми пятнами");
        animalCard.setGeneticDiseases("Наследственных заболеваний не выявлено");
        animalCard.setDescription("Очень дружелюбный и игривый кот");
        animalCard.setGoal(goal);
        animalCard.setCost(new BigDecimal("15000"));
        animalCard.setCreated(LocalDateTime.now());
        animalCard.setUpdated(LocalDateTime.now());
        animalCard.setStatus(status);
        return animalCard;
    }

    public static CreateAnimalCardRequest createValidAnimalCardRequest() {
        CreateAnimalCardRequest request = new CreateAnimalCardRequest();
        request.setName("Барсик");
        request.setSpeciesId(1L);
        request.setGoal("SELL");
        request.setCost(new BigDecimal("15000"));
        request.setHasBreed(true);
        request.setBreed("Британская короткошерстная");
        request.setGender(org.nsu.animal.dto.enums.Gender.M);
        request.setBirthday(LocalDate.of(2023, 5, 15));
        request.setWeight(new BigDecimal("4.5"));
        request.setColor("Серый с белыми пятнами");
        request.setGeneticDiseases("Наследственных заболеваний не выявлено");
        request.setDescription("Очень дружелюбный и игривый кот");
        return request;
    }

    public static CreateAnimalCardRequest createInvalidAnimalCardRequest() {
        CreateAnimalCardRequest request = new CreateAnimalCardRequest();
        request.setName(""); // Невалидное имя
        request.setSpeciesId(1L);
        request.setGoal("SELL");
        request.setHasBreed(false);
        request.setGender(org.nsu.animal.dto.enums.Gender.M);
        request.setBirthday(LocalDate.of(2023, 5, 15));
        request.setColor("Серый");
        request.setGeneticDiseases("Нет заболеваний");
        return request;
    }

    public static CreateAnimalCardRequest createAnimalCardRequestWithoutBreed() {
        CreateAnimalCardRequest request = new CreateAnimalCardRequest();
        request.setName("Мурка");
        request.setSpeciesId(1L);
        request.setGoal("SELL");
        request.setHasBreed(false);
        request.setGender(org.nsu.animal.dto.enums.Gender.F);
        request.setBirthday(LocalDate.of(2022, 3, 10));
        request.setColor("Рыжий");
        request.setGeneticDiseases("Здорова");
        request.setDescription("Ласковая кошечка");
        return request;
    }

    public static UpdateAnimalCardRequest createValidUpdateAnimalCardRequest() {
        UpdateAnimalCardRequest request = new UpdateAnimalCardRequest();
        request.setName("Барсик Обновленный");
        request.setSpeciesId(1L);
        request.setGoal("SELL");
        request.setHasBreed(true);
        request.setBreed("Шотландская вислоухая");
        request.setGender(org.nsu.animal.dto.enums.Gender.M);
        request.setBirthday(LocalDate.of(2023, 8, 20));
        request.setWeight(new BigDecimal("6.2"));
        request.setColor("Черный с белыми пятнами");
        request.setGeneticDiseases("Наследственных заболеваний не выявлено");
        request.setDescription("Обновленное описание кота");
        return request;
    }

    public static UpdateAnimalCardRequest createInvalidUpdateAnimalCardRequest() {
        UpdateAnimalCardRequest request = new UpdateAnimalCardRequest();
        request.setName("");
        request.setSpeciesId(1L);
        request.setGoal("SELL");
        request.setHasBreed(false);
        request.setGender(org.nsu.animal.dto.enums.Gender.M);
        request.setBirthday(LocalDate.of(2023, 5, 15));
        request.setColor("Серый");
        request.setGeneticDiseases("Нет заболеваний");
        return request;
    }
}
