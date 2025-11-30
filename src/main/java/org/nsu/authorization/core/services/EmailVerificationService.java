package org.nsu.authorization.core.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.nsu.authorization.core.dto.requests.EmailVerificationRequest;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
import org.nsu.authorization.core.utils.JwtClaimKey;
import org.nsu.users.core.repositories.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.nsu.users.entity.User;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationSenderService emailVerificationSenderService;
    private final VerificationCodeCachingService verificationCodeCachingService;

    /**
     * Verifies an email using a code, based on the authenticated user from the JWT.
     *
     * @param dto The request DTO containing the verification code.
     * @param email The user email address, that will be used to email.
     */
    public void verifyEmail(EmailVerificationRequest dto, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailVerificationFailException("User not found with email: " + email));

        String userId = String.valueOf(user.getId());

        String cachedCode = verificationCodeCachingService.getCode(userId);

        if (cachedCode == null) { // if the code is expired, generate a new one and send it
            cachedCode = verificationCodeCachingService.generateAndCacheCode(userId);
            emailVerificationSenderService.send(email, cachedCode);
            throw new EmailVerificationFailException("Неверный код или срок действия истёк. Мы выслали новое письмо для подтверждения почты");
        }

        // check if the code matches the one from the cache
        if (Objects.equals(cachedCode, dto.getCode())) {
            user.setEmailVerified(true);
            userRepository.save(user);
            return;
        }
        throw new EmailVerificationFailException("Failed to verify email: invalid verification code.");
    }
}