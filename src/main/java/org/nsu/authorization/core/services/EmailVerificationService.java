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
     * @param jwt The parsed JWT token provided by Spring Security.
     */
    public void verifyEmail(EmailVerificationRequest dto, Jwt jwt) {

        String userId;
        String emailDst;
        try {
            userId = jwt.getClaimAsString(JwtClaimKey.USER_ID);
            emailDst = jwt.getClaimAsString(JwtClaimKey.USER_EMAIL);
        } catch (Exception e) {
            throw new EmailVerificationFailException("Failed to extract claims from token: " + e.getMessage(), e);
        }

        if (userId == null || emailDst == null) {
            throw new EmailVerificationFailException(
                    "Failed to verify email: Token is missing user ID or email claims.");
        }

        if (!NumberUtils.isParsable(userId)) {
            throw new EmailVerificationFailException("Failed to verify email: user id must only contain digits.");
        }

        String cachedCode = verificationCodeCachingService.getCode(userId);

        if (cachedCode == null) { // if the code is expired, generate a new one and send it
            cachedCode = verificationCodeCachingService.generateAndCacheCode(userId);
            emailVerificationSenderService.send(emailDst, cachedCode);
            return;
        }

        // check if the code matches the one from the cache
        if (Objects.equals(cachedCode, dto.getCode())) {
            User user;
            try {
                user = userRepository.getReferenceById(Long.parseLong(userId));
            } catch (EntityNotFoundException e) {
                throw new EmailVerificationFailException("Failed to verify email: " + e.getMessage(), e);
            }
            user.setEmailVerified(true);
            userRepository.save(user);
            return;
        }
        throw new EmailVerificationFailException("Failed to verify email: invalid verification code.");
    }
}