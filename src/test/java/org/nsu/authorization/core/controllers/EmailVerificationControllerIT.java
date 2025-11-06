package org.nsu.authorization.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.nsu.authorization.core.dto.requests.EmailVerificationRequest;
import org.nsu.authorization.core.utils.CacheUtil;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.authorization.core.utils.VerificationCodeGenerator;
import org.nsu.users.entity.User; // Убедитесь, что этот импорт корректен
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = EmailVerificationControllerIT.RedisInitializer.class)
class EmailVerificationControllerIT {

    // --- Конфигурация Testcontainers для Redis ---
    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    static class RedisInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            redis.start();
            TestPropertyValues.of(
                    "spring.data.redis.host=" + redis.getHost(),
                    "spring.data.redis.port=" + redis.getMappedPort(6379)
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    // --- Зависимости ---
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    // --- Моки ---
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JavaMailSender javaMailSender; // Мокируем, чтобы не слать реальные письма

    @MockBean
    private VerificationCodeGenerator verificationCodeGenerator; // Мокируем для предсказуемости кода

    // --- Тестовые данные ---
    private static final String TEST_USER_ID = "123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String VALID_CODE = "123456";
    private static final String INVALID_CODE = "789012";
    private static final String NEW_GENERATED_CODE = "345678";
    private static final String CACHE_KEY = "user:123:email:code";

    @BeforeEach
    void setUp() {
        // Очищаем кэш Redis перед каждым тестом
        Optional.ofNullable(cacheManager.getCache(CacheUtil.VERIFICATION_CODE_CACHE_NAME)).ifPresent(Cache::clear);

        // Сбрасываем моки
        reset(userRepository, javaMailSender, verificationCodeGenerator);
    }

    /**
     * Создает DTO запроса
     */
    private EmailVerificationRequest createRequest(String code) {
        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setCode(code);
        return request;
    }

    /**
     * Создает мок JWT токена
     */
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithClaims() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .claim("userID", TEST_USER_ID)
                        .claim("email", TEST_EMAIL)
                );
    }

    // -----------------------------------------------------------------
    // --- СЦЕНАРИИ ТЕСТОВ ---
    // -----------------------------------------------------------------

    @Test
    @DisplayName("1. Успех: Код в Redis верный, пользователь найден, email верифицирован")
    void testVerifyEmail_Success() throws Exception {
        // --- Arrange ---
        // 1. Помещаем правильный код в кэш
        cacheManager.getCache(CacheUtil.VERIFICATION_CODE_CACHE_NAME).put(CACHE_KEY, VALID_CODE);

        // 2. Готовим мок пользователя
        User mockUser = new User();
        mockUser.setId(Long.parseLong(TEST_USER_ID));
        mockUser.setEmailVerified(false);

        // 3. Настраиваем мок репозитория
        when(userRepository.getReferenceById(Long.parseLong(TEST_USER_ID))).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailVerificationRequest request = createRequest(VALID_CODE);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Ожидаем 200 OK

        // --- Verify ---
        // 1. Убеждаемся, что пользователя искали в БД
        verify(userRepository).getReferenceById(Long.parseLong(TEST_USER_ID));

        // 2. Убеждаемся, что пользователя сохранили с флагом isEmailVerified = true
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getId().equals(Long.parseLong(TEST_USER_ID)) && savedUser.isEmailVerified()
        ));

        // 3. Убеждаемся, что новое письмо не отправлялось
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("2. Записи в Redis нет: Генерируется новый код и отправляется письмо")
    void testVerifyEmail_CodeNotInRedis_ResendsEmail() throws Exception {
        when(verificationCodeGenerator.generateVerificationCode()).thenReturn(NEW_GENERATED_CODE);
        EmailVerificationRequest request = createRequest("any-code-will-do");

        // --- Act ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // --- Verify ---
        // 1. Создаем "ловушку" для SimpleMailMessage
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // 2. Убеждаемся, что send был вызван, и "захватываем" аргумент
        verify(javaMailSender).send(captor.capture());

        // 3. Получаем захваченный объект и проверяем его
        SimpleMailMessage sentMessage = captor.getValue();
        Assertions.assertNotNull(sentMessage.getTo());
        assertThat(sentMessage.getTo()[0]).isEqualTo(TEST_EMAIL);
        assertThat(sentMessage.getText()).contains(NEW_GENERATED_CODE);

        // 2. Убеждаемся, что БД не трогали (пользователя не искали и не сохраняли)
        verify(userRepository, never()).getReferenceById(anyLong());
        verify(userRepository, never()).save(any(User.class));

        // 3. Убеждаемся, что новый код появился в кэше
        String cachedCode = Objects.requireNonNull(cacheManager.getCache(CacheUtil.VERIFICATION_CODE_CACHE_NAME)).get(CACHE_KEY, String.class);
        assertThat(cachedCode).isEqualTo(NEW_GENERATED_CODE);
    }

    @Test
    @DisplayName("3. Коды не совпадают: Запись в Redis есть, но код в запросе неверный")
    void testVerifyEmail_CodeMismatch_ReturnsError() throws Exception {
        // --- Arrange ---
        // 1. Помещаем правильный (ожидаемый) код в кэш
        cacheManager.getCache(CacheUtil.VERIFICATION_CODE_CACHE_NAME).put(CACHE_KEY, VALID_CODE);

        // 2. Создаем запрос с НЕПРАВИЛЬНЫМ кодом
        EmailVerificationRequest request = createRequest(INVALID_CODE);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Ожидаем ошибку (400 или 500, в зависимости от @ControllerAdvice)
                // Предполагаем, что EmailVerificationFailException маппится на 400
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("invalid verification code")));

        // --- Verify ---
        // 1. Убеждаемся, что БД не трогали
        verify(userRepository, never()).getReferenceById(anyLong());
        verify(userRepository, never()).save(any(User.class));

        // 2. Убеждаемся, что письмо не отправлялось
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("4. База недоступна: Коды совпадают, но БД кидает ошибку при поиске")
    void testVerifyEmail_CodeMatches_DatabaseError() throws Exception {
        // --- Arrange ---
        // 1. Помещаем правильный код в кэш
        cacheManager.getCache(CacheUtil.VERIFICATION_CODE_CACHE_NAME).put(CACHE_KEY, VALID_CODE);

        // 2. Настраиваем мок репозитория на ошибку
        when(userRepository.getReferenceById(Long.parseLong(TEST_USER_ID)))
                .thenThrow(new EntityNotFoundException("Test DB Error: User not found"));

        EmailVerificationRequest request = createRequest(VALID_CODE);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Сервис ловит EntityNotFoundException и кидает EmailVerificationFailException
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Test DB Error: User not found")));

        // --- Verify ---
        // 1. Убеждаемся, что БЫЛА попытка обратиться к БД
        verify(userRepository).getReferenceById(Long.parseLong(TEST_USER_ID));

        // 2. Убеждаемся, что сохранения не было
        verify(userRepository, never()).save(any(User.class));

        // 3. Убеждаемся, что письмо не отправлялось
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

}