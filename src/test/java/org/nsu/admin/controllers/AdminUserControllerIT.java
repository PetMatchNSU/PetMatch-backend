package org.nsu.admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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


import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminUserControllerIT extends AbstractIntegrityTest {

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

    private User moderatorUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        // Clean up
        statusCommentRepository.deleteAll();
        userRepository.deleteAll();
        authorityRepository.deleteAll();
        statusRepository.deleteAll();
        regionRepository.deleteAll();

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
        moderatorUserTemp.setLastName("Admin");
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
        regularUserTemp.setLastName("Test");
        regularUserTemp.setPassword("password");
        regularUserTemp.setGender(Gender.F);
        regularUserTemp.setEmailVerified(true);
        regularUserTemp.setStatus(activeStatus);
        regularUserTemp.setRegion(region);
        regularUserTemp.setAuthorities(Set.of(savedUserAuthority));
        regularUser = userRepository.save(regularUserTemp);

   }

    @Test
    void testGetUsersList_success() throws Exception {
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
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);

        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(personDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testSetUserStatus_success() throws Exception {
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
}
