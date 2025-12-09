package org.nsu.admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private User moderatorUser;
    private User regularUser;
    private AnimalCard testCard;

    @BeforeEach
    void setUp() {
        // Clean up
        animalCardRepository.deleteAll();
        animalRepository.deleteAll();
        placementGoalRepository.deleteAll();
        animalCardStatusRepository.deleteAll();
        userRepository.deleteAll();
        authorityRepository.deleteAll();

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
    void testSetCardStatus_success() throws Exception {
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Lock the card
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/lock", testCard.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Set status (this will test if Redis lock state is maintained)
        mockMvc.perform(post("/api/v1/admin/cards/{cardId}/status", testCard.getId())
                .param("targetStatus", "Approved")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }
}
