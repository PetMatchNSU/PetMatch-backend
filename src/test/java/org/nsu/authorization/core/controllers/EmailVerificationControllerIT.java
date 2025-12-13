package org.nsu.authorization.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nsu.authorization.core.dto.requests.EmailVerificationRequest;
import org.nsu.testutils.AbstractIntegrityTest;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class EmailVerificationControllerIT extends AbstractIntegrityTest {

    // --- Зависимости ---
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Моки ---
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private org.nsu.authorization.core.services.VerificationCodeCachingService verificationCodeCachingService; // Мокируем кэширование кодов

    @MockBean
    private org.nsu.authorization.core.services.EmailVerificationSenderService emailVerificationSenderService; // Мокируем отправку писем

    // --- Тестовые данные ---
    private static final String TEST_USER_ID = "123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String VALID_CODE = "123456";
    private static final String INVALID_CODE = "789012";
    private static final String NEW_GENERATED_CODE = "345678";

    @BeforeEach
    void setUp() {
        // Сбрасываем моки
        reset(userRepository, verificationCodeCachingService, emailVerificationSenderService);
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
     * Создает мок JWT токена с email в качестве subject
     */
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithClaims() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject(TEST_EMAIL)  // principal.getName() будет возвращать email
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
        // 1. Готовим мок пользователя
        User mockUser = new User();
        mockUser.setId(Long.parseLong(TEST_USER_ID));
        mockUser.setEmailVerified(false);

        // 2. Настраиваем мок репозитория: findByEmail вместо getReferenceById
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(java.util.Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Настраиваем мок кэша: код есть и он верный
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);

        EmailVerificationRequest request = createRequest(VALID_CODE);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Ожидаем 200 OK

        // --- Verify ---
        // 1. Убеждаемся, что пользователя искали в БД по email
        verify(userRepository).findByEmail(TEST_EMAIL);

        // 2. Убеждаемся, что код был получен из кэша
        verify(verificationCodeCachingService).getCode(TEST_USER_ID);

        // 3. Убеждаемся, что пользователя сохранили с флагом isEmailVerified = true
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getId().equals(Long.parseLong(TEST_USER_ID)) && savedUser.isEmailVerified()
        ));

        // 4. Убеждаемся, что новое письмо не отправлялось
        verify(emailVerificationSenderService, never()).send(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("2. Записи в Redis нет: Генерируется новый код и отправляется письмо")
    void testVerifyEmail_CodeNotInRedis_ResendsEmail() throws Exception {
        // --- Arrange ---
        // 1. Готовим мок пользователя
        User mockUser = new User();
        mockUser.setId(Long.parseLong(TEST_USER_ID));
        mockUser.setEmailVerified(false);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(java.util.Optional.of(mockUser));

        // 2. Настраиваем мок кэша: кода нет (null)
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(null);
        when(verificationCodeCachingService.generateAndCacheCode(TEST_USER_ID)).thenReturn(NEW_GENERATED_CODE);

        // 3. Mock the email sender to not actually send emails
        org.mockito.Mockito.doNothing().when(emailVerificationSenderService).send(TEST_EMAIL, NEW_GENERATED_CODE);

        EmailVerificationRequest request = createRequest("189898");

        // --- Act ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // --- Verify ---
        // 1. Убеждаемся, что пользователя искали в БД по email
        verify(userRepository).findByEmail(TEST_EMAIL);

        // 2. Убеждаемся, что код был запрошен из кэша и его не было
        verify(verificationCodeCachingService).getCode(TEST_USER_ID);

        // 3. Убеждаемся, что новый код был сгенерирован
        verify(verificationCodeCachingService).generateAndCacheCode(TEST_USER_ID);

        // 4. Убеждаемся, что письмо было отправлено через сервис
        verify(emailVerificationSenderService).send(TEST_EMAIL, NEW_GENERATED_CODE);

        // 5. Убеждаемся, что БД не трогали (пользователя не сохраняли)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("3. Коды не совпадают: Запись в Redis есть, но код в запросе неверный")
    void testVerifyEmail_CodeMismatch_ReturnsError() throws Exception {
        // --- Arrange ---
        // 1. Готовим мок пользователя
        User mockUser = new User();
        mockUser.setId(Long.parseLong(TEST_USER_ID));
        mockUser.setEmailVerified(false);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(java.util.Optional.of(mockUser));

        // 2. Настраиваем мок кэша: правильный код есть в кэше
        when(verificationCodeCachingService.getCode(TEST_USER_ID)).thenReturn(VALID_CODE);

        // 3. Создаем запрос с НЕПРАВИЛЬНЫМ кодом
        EmailVerificationRequest request = createRequest(INVALID_CODE);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Ожидаем ошибку 400 с сообщением об ошибке
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // --- Verify ---
        // 1. Убеждаемся, что пользователя искали в БД
        verify(userRepository).findByEmail(TEST_EMAIL);

        // 2. Убеждаемся, что код был получен из кэша
        verify(verificationCodeCachingService).getCode(TEST_USER_ID);

        // 3. Убеждаемся, что БД не трогали (пользователя не сохраняли)
        verify(userRepository, never()).save(any(User.class));

        // 4. Убеждаемся, что письмо не отправлялось
        verify(emailVerificationSenderService, never()).send(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("4. База недоступна: Коды совпадают, но БД кидает ошибку при поиске")
    void testVerifyEmail_CodeMatches_DatabaseError() throws Exception {
        // --- Arrange ---
        // 1. Настраиваем мок репозитория на ошибку при поиске по email
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenThrow(new EntityNotFoundException("Test DB Error: User not found"));

        EmailVerificationRequest request = createRequest(VALID_CODE);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/user/verify-email")
                        .with(jwtWithClaims())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Сервис ловит EntityNotFoundException и кидает EmailVerificationFailException
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // --- Verify ---
        // 1. Убеждаемся, что БЫЛА попытка обратиться к БД
        verify(userRepository).findByEmail(TEST_EMAIL);

        // 2. Убеждаемся, что сохранения не было
        verify(userRepository, never()).save(any(User.class));

        // 3. Убеждаемся, что письмо не отправлялось
        verify(emailVerificationSenderService, never()).send(any(String.class), any(String.class));
    }

}
