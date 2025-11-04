package org.nsu.authorization.core.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.EmailVerifierRequest;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
import org.nsu.users.core.repositories.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.nsu.users.entity.User;

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
    public void verifyEmail(EmailVerifierRequest dto, Jwt jwt) {

        String userId;
        String emailDst;
        try {
            userId = jwt.getClaimAsString("userID");
            emailDst = jwt.getClaimAsString("email");

            if (userId == null || emailDst == null) {
                throw new EmailVerificationFailException(
                        "Failed to verify email: Token is missing 'userID' or 'email' claims.");
            }
        } catch (Exception e) {
            throw new EmailVerificationFailException("Failed to extract claims from token: " + e.getMessage());
        }

        String cachedCode = verificationCodeCachingService.getCode(userId);

        if (cachedCode == null) { // if the code is expired, generate a new one and send it
            cachedCode = verificationCodeCachingService.generateAndCacheCode(userId);
            emailVerificationSenderService.Send(emailDst, cachedCode);
            return;
        }

        Long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new EmailVerificationFailException("Failed to verify email: userid must only contain digits.");
        }

        // check if the code matches the one from the cache
        if (cachedCode.equals(dto.getCode())) {
            User user;
            try {
                user = userRepository.getReferenceById(id);
            } catch (EntityNotFoundException e) {
                throw new EmailVerificationFailException("Failed to verify email: " + e.getMessage());
            }
            user.setEmailVerified(true);
            userRepository.save(user);
            return;
        }
        throw new EmailVerificationFailException("Failed to verify email: invalid verification code.");
    }
}