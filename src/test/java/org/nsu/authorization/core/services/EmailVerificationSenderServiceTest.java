package org.nsu.authorization.core.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailVerificationSenderService emailService;

    @Test
    void testSend() {
        String testFromEmail = "no-reply@petmatch.com";
        String testToEmail = "user@test.com";
        String testCode = "ABCDEF";

        // Manually set the @Value("${spring.mail.username}") field
        // This is necessary because the Spring context isn't running
        ReflectionTestUtils.setField(emailService, "from", testFromEmail);

        String expectedSubject = "Email Verification";
        String expectedText = "Регистрация на платформе Pet Match почти закончена! Пожалуйста, подтвердите свою электронную почту, введя следующий временный код на последнем шаге регистрации: "
                + testCode
                + ". Спасибо, что Вы с нами! Ваш Pet Match.";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.Send(testToEmail, testCode);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(testFromEmail, sentMessage.getFrom());
        assertEquals(testToEmail, sentMessage.getTo()[0]);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedText, sentMessage.getText());
    }
}