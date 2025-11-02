package org.nsu.authorization.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationSenderService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String from;

	private static final String subject = "Email Verification";
	private static final String textPart1 = "Регистрация на платформе Pet Match почти закончена! Пожалуйста, подтвердите свою электронную почту, введя следующий временный код на последнем шаге регистрации: ";
	private static final String textPart2 = ". Спасибо, что Вы с нами! Ваш Pet Match.";

	public void Send(String to, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(textPart1 + code + textPart2);
		message.setFrom(from);

		mailSender.send(message);
	}
}
