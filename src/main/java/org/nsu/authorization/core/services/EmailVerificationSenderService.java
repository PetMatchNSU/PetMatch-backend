package org.nsu.authorization.core.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationSenderService {

    private final JavaMailSender mailSender;

    private final String from;

    private static final String subject = "Email Verification";
    private static final String TEXT_TEMPLATE = "Регистрация на платформе Pet Match почти закончена! Пожалуйста, подтвердите свою электронную почту, введя следующий временный код на последнем шаге регистрации: %s. Спасибо, что Вы с нами! Ваш Pet Match.";

    public EmailVerificationSenderService(JavaMailSender mailSender, @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(String.format(TEXT_TEMPLATE, code));
        message.setFrom(from);

        mailSender.send(message);
    }
}