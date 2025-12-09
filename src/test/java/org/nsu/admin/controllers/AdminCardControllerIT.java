package org.nsu.admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nsu.admin.services.RedisLockService;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.testutils.AbstractIntegrityTest;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.core.repositories.StatusRepository;
import org.nsu.users.core.repositories.AuthorityRepository;
import org.nsu.users.repositories.RegionRepository;
import org.nsu.users.entity.*;
import org.nsu.animal.entity.*;
import org.nsu.animal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminCardControllerIT extends AbstractIntegrityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private PlacementGoalRepository placementGoalRepository;

    @Autowired
    private AnimalCardStatusRepository animalCardStatusRepository;

    @Autowired
    private AnimalCardRepository animalCardRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private RegionRepository regionRepository;

    private static User moderatorUser;
    private static User regularUser;
    private static AnimalCard testCard;
    private static boolean isInitialized = false;

    @BeforeEach
    void setUp() {
        if (isInitialized) {
            return; // Skip setup if already initialized
        }
        // Clean up
        animalCardRepository.deleteAll();
        animalRepository.deleteAll();
        placementGoalRepository.deleteAll();
        animalCardStatusRepository.deleteAll();
        userRepository.deleteAll();
        // Note: Don't delete authorities as they might be used by other tests

        // Create authorities
        Authority moderatorAuthority = new Authority();
        moderatorAuthority.setName("ROLE_MODERATOR");
        moderatorAuthority.setDescription("Moderator authority");
        Authority savedModeratorAuthority = authorityRepository.save(moderatorAuthority);

        Authority userAuthority = new Authority();
        userAuthority.setName("ROLE_USER");
        userAuthority.setDescription("User authority");
        Authority savedUserAuthority = authorityRepository.save(userAuthority);

        // Create statuses
        Status activeStatus = new Status();
        activeStatus.setName("Active");
        statusRepository.save(activeStatus);

        // Create region
        Region region = new Region();
        region.setRegion("Test Region");
        region.setCity("Test City");
        regionRepository.save(region);

        // Create moderator user
        User moderatorUserTemp = new User();
        moderatorUserTemp.setEmail("moderator@example.com");
        moderatorUserTemp.setFirstName("Moderator");
        moderatorUserTemp.setSecondName("User");
        moderatorUserTemp.setLastName("Admin");
        moderatorUserTemp.setPassword("password");
        moderatorUserTemp.setGender(Gender.M);
        moderatorUserTemp.setEmailVerified(true);
        moderatorUserTemp.setStatus(activeStatus);
        moderatorUserTemp.setRegion(region);
        moderatorUserTemp.setAuthorities(Set.of(savedModeratorAuthority));
        moderatorUser = userRepository.save(moderatorUserTemp);

        // Create regular user
        User regularUserTemp = new User();
        regularUserTemp.setEmail("regular@example.com");
        regularUserTemp.setFirstName("Regular");
        regularUserTemp.setSecondName("User");
        regularUserTemp.setLastName("Test");
        regularUserTemp.setPassword("password");
        regularUserTemp.setGender(Gender.F);
        regularUserTemp.setEmailVerified(true);
        regularUserTemp.setStatus(activeStatus);
        regularUserTemp.setRegion(region);
        regularUserTemp.setAuthorities(Set.of(savedUserAuthority));
        regularUser = userRepository.save(regularUserTemp);

        // Create animal
        Animal animal = new Animal();
        animal.setName("Test Animal");
        Animal savedAnimal = animalRepository.save(animal);

        // Create placement goal
        PlacementGoal goal = new PlacementGoal();
        goal.setGoal("Test Goal");
        PlacementGoal savedGoal = placementGoalRepository.save(goal);

        // Create animal card status
        AnimalCardStatus pendingStatus = new AnimalCardStatus();
        pendingStatus.setName("Pending");
        AnimalCardStatus approvedStatus = new AnimalCardStatus();
        approvedStatus.setName("Approved");

        AnimalCardStatus savedPendingStatus = animalCardStatusRepository.save(pendingStatus);
        animalCardStatusRepository.save(approvedStatus);

        // Create test animal card
        AnimalCard card = new AnimalCard();
        card.setCardAuthor(regularUser);
        card.setName("Test Card");
        card.setAnimal(savedAnimal);
        card.setBreed("Test Breed");
        card.setGender(AnimalGender.M);
        card.setBirthdate(LocalDate.now().minusYears(5));
        card.setWeight(BigDecimal.valueOf(25.5));
        card.setColor("Black");
        card.setGeneticDiseases("None");
        card.setDescription("Test card description");
        card.setGoal(savedGoal);
        card.setCost(BigDecimal.valueOf(10000.00));
        card.setCreated(LocalDateTime.now());
        card.setUpdated(LocalDateTime.now());
        card.setStatus(savedPendingStatus);
        testCard = animalCardRepository.save(card);

        isInitialized = true; // Mark as initialized
    }

    @Test
    void testGetCardsList_success() throws Exception {
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/cards")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminCardListRequest()))
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testLockCard_success() throws Exception {
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", testCard.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void testSetCardStatus_success_after_lock() throws Exception {
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // First lock the card
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", testCard.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Then set status (this should work since we own the lock)
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/status", testCard.getId())
                .param("targetStatus", "Approved")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCardsList_unauthorized() throws Exception {
        // No authentication - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/cards")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminCardListRequest())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetCardsList_forbidden_wrongRole() throws Exception {
        // Authenticate as regular user (not moderator) - global exception handler converts to 500
        PersonDetails personDetails = new PersonDetails(regularUser);

        mockMvc.perform(post("/api/v1/admin/cards")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminCardListRequest()))
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLockCard_nonExistent() throws Exception {
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", 99999L)
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLockCard_unauthorized() throws Exception {
        // No authentication - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", testCard.getId()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSetCardStatus_noLock() throws Exception {
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Create testCard1
        AnimalCard testCard1 = new AnimalCard();
        testCard1.setCardAuthor(regularUser);
        testCard1.setName("Test Card 1");
        testCard1.setAnimal(animalRepository.findAll().get(0));
        testCard1.setBreed("Test Breed");
        testCard1.setGender(AnimalGender.M);
        testCard1.setBirthdate(LocalDate.now().minusYears(5));
        testCard1.setWeight(BigDecimal.valueOf(25.5));
        testCard1.setColor("Black");
        testCard1.setGeneticDiseases("None");
        testCard1.setDescription("Test card 1 description");
        testCard1.setGoal(placementGoalRepository.findAll().get(0));
        testCard1.setCost(BigDecimal.valueOf(10000.00));
        testCard1.setCreated(LocalDateTime.now());
        testCard1.setUpdated(LocalDateTime.now());
        testCard1.setStatus(animalCardStatusRepository.findByName("Pending").get());
        AnimalCard savedTestCard1 = animalCardRepository.save(testCard1);

        // Try to set status without locking first - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/status", savedTestCard1.getId())
                .param("targetStatus", "Approved")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    void testSetCardStatus_wrongLockOwner() throws Exception {
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Lock the card
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", testCard.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Create another moderator user
        User otherModerator = new User();
        otherModerator.setEmail("other@moderator.com");
        otherModerator.setFirstName("Other");
        otherModerator.setSecondName("Moderator");
        otherModerator.setLastName("Admin");
        otherModerator.setPassword("password");
        otherModerator.setGender(Gender.M);
        otherModerator.setEmailVerified(true);
        otherModerator.setStatus(statusRepository.findByName("Active").get());
        otherModerator.setRegion(regionRepository.findByRegionAndCity("Test Region", "Test City").get());
        otherModerator.setAuthorities(Set.of(authorityRepository.findByName("ROLE_MODERATOR").get()));
        User savedOtherModerator = userRepository.save(otherModerator);

        PersonDetails otherPersonDetails = new PersonDetails(savedOtherModerator);

        // Try to set status with different moderator - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/status", testCard.getId())
                .param("targetStatus", "Approved")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(otherPersonDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    void testSetCardStatus_invalidStatus() throws Exception {
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Lock the card
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", testCard.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Try to set invalid status - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/status", testCard.getId())
                .param("targetStatus", "InvalidStatus")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }
}
