package org.nsu.admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.admin.services.RedisLockService;
import org.nsu.admin.repositories.StatusCommentRepository;
import org.nsu.admin.dto.LockModel;
import org.nsu.admin.dto.LockType;
import org.nsu.testutils.AbstractIntegrityTest;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.core.repositories.StatusRepository;
import org.nsu.users.core.repositories.AuthorityRepository;
import org.nsu.users.repositories.RegionRepository;
import org.nsu.users.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;



import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class) // because the testSetCardStatus_noLock test must be first
public class AdminUserControllerIT extends AbstractIntegrityTest {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserControllerIT.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private StatusCommentRepository statusCommentRepository;

    @Autowired
    private RegionRepository regionRepository;

    private static User moderatorUser;
    private static User regularUser;
    private static boolean isInitialized = false;

    @BeforeEach
    void setUp() {
        if (isInitialized) {
            return; // Skip setup if already initialized
        }
        // Clean up
        statusCommentRepository.deleteAll();
        userRepository.deleteAll();
        statusRepository.deleteAll();
        authorityRepository.deleteAll();
        regionRepository.deleteAll();
        // Note: Don't delete authorities and statuses as they might be used by other tests

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

        Status blockedStatus = new Status();
        blockedStatus.setName("Blocked");
        statusRepository.save(blockedStatus);

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
        moderatorUserTemp.setMiddleName("Admin");
        moderatorUserTemp.setPassword("password");
        moderatorUserTemp.setGender(Gender.M);
        moderatorUserTemp.setEmailVerified(true);
        moderatorUserTemp.setStatus(activeStatus);
        moderatorUserTemp.setRegion(region);
        moderatorUserTemp.setAuthorities(Set.of(savedModeratorAuthority));
        moderatorUser = userRepository.save(moderatorUserTemp);

        // Create regular user to moderate
        User regularUserTemp = new User();
        regularUserTemp.setEmail("regular@example.com");
        regularUserTemp.setFirstName("Regular");
        regularUserTemp.setSecondName("User");
        regularUserTemp.setMiddleName("Test");
        regularUserTemp.setPassword("password");
        regularUserTemp.setGender(Gender.F);
        regularUserTemp.setEmailVerified(true);
        regularUserTemp.setStatus(activeStatus);
        regularUserTemp.setRegion(region);
        regularUserTemp.setAuthorities(Set.of(savedUserAuthority));
        regularUser = userRepository.save(regularUserTemp);

        isInitialized = true; // Mark as initialized
    }

    @Test
    void testGetUsersList_success() throws Exception {
        logger.info("Starting testGetUsersList_success");
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminUserListRequest()))
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testLockUser_success() throws Exception {
        logger.info("Starting testLockUser_success");
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testSetUserStatus_success() throws Exception {
        logger.info("Starting testSetUserStatus_success");
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Lock
        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Then set status
        mockMvc.perform(post("/api/v1/admin/users/{userId}/status", regularUser.getId())
                .param("targetStatus", "Blocked")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    void testSetUserStatus_noLock() throws Exception {
        logger.info("Starting testSetUserStatus_noLock");
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Try to set status without locking first - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/users/{userId}/status", regularUser.getId())
                .param("targetStatus", "Blocked")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetUsersList_unauthorized() throws Exception {
        logger.info("Starting testGetUsersList_unauthorized");
        // No authentication - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminUserListRequest())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetUsersList_forbidden_wrongRole() throws Exception {
        logger.info("Starting testGetUsersList_forbidden_wrongRole");
        // Authenticate as regular user (not moderator) - global exception handler converts to 500
        PersonDetails personDetails = new PersonDetails(regularUser);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminUserListRequest()))
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLockUser_nonExistent() throws Exception {
        logger.info("Starting testLockUser_nonExistent");
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", 99999L)
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLockUser_unauthorized() throws Exception {
        logger.info("Starting testLockUser_unauthorized");
        // No authentication - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSetUserStatus_wrongLockOwner() throws Exception {
        logger.info("Starting testSetUserStatus_wrongLockOwner");
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Lock the user
        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Create another moderator user
        User otherModerator = new User();
        otherModerator.setEmail("other@moderator.com");
        otherModerator.setFirstName("Other");
        otherModerator.setSecondName("Moderator");
        otherModerator.setMiddleName("Admin");
        otherModerator.setPassword("password");
        otherModerator.setGender(Gender.M);
        otherModerator.setEmailVerified(true);
        otherModerator.setStatus(statusRepository.findByName("Active").get());
        otherModerator.setRegion(regionRepository.findByRegionAndCity("Test Region", "Test City").get());
        otherModerator.setAuthorities(Set.of(authorityRepository.findByName("ROLE_MODERATOR").get()));
        User savedOtherModerator = userRepository.save(otherModerator);

        PersonDetails otherPersonDetails = new PersonDetails(savedOtherModerator);

        // Try to set status with different moderator - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/users/{userId}/status", regularUser.getId())
                .param("targetStatus", "Blocked")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(otherPersonDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSetUserStatus_invalidStatus() throws Exception {
        logger.info("Starting testSetUserStatus_invalidStatus");
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        // Lock the user
        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());

        // Try to set invalid status - global exception handler converts to 500
        mockMvc.perform(post("/api/v1/admin/users/{userId}/status", regularUser.getId())
                .param("targetStatus", "InvalidStatus")
                .param("reason", "Test reason")
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isInternalServerError());
    }
}
