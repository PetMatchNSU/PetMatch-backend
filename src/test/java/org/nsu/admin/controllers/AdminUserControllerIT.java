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
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = AdminUserControllerIT.RedisInitializer.class)
public class AdminUserControllerIT extends AbstractIntegrityTest {

    @MockitoBean
    private RedisLockService redisLockService;

    // --- Конфигурация Testcontainers для Redis ---
    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    static class RedisInitializer implements org.springframework.context.ApplicationContextInitializer<org.springframework.context.ConfigurableApplicationContext> {
        public void initialize(org.springframework.context.ConfigurableApplicationContext configurableApplicationContext) {
            redis.start();
            TestPropertyValues.of(
                    "spring.data.redis.host=" + redis.getHost(),
                    "spring.data.redis.port=" + redis.getMappedPort(6379)
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

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

        // Mock RedisLockService for tests
        when(redisLockService.setLock(any(Long.class), any(Long.class), eq(LockType.USER))).thenReturn(true);
        when(redisLockService.getLock(any(Long.class), eq(LockType.USER))).thenReturn(null);
        when(redisLockService.releaseLock(any(Long.class), eq(LockType.USER))).thenReturn(true);
    }

    @Test
    void testGetUsersList_success() throws Exception {
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new org.nsu.admin.dto.AdminUserListRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void testLockUser_success() throws Exception {
        // Authenticate as moderator
        PersonDetails personDetails = new PersonDetails(moderatorUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void testSetUserStatus_success() throws Exception {
        // First lock the user
        PersonDetails personDetails = new PersonDetails(moderatorUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lock
        mockMvc.perform(post("/api/v1/admin/users/{userId}/lock", regularUser.getId()))
                .andExpect(status().isOk());

        // Mock getLock for the locked user
        when(redisLockService.getLock(eq(regularUser.getId()), eq(LockType.USER))).thenReturn(new LockModel(regularUser.getId(), moderatorUser.getId(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)));

        // Then set status
        mockMvc.perform(post("/api/v1/admin/users/{userId}/status", regularUser.getId())
                .param("targetStatus", "Blocked")
                .param("reason", "Test reason"))
                .andExpect(status().isOk());
    }
}
