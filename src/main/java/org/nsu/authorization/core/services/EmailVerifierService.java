package org.nsu.authorization.core.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.EmailVerifierRequest;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
import org.nsu.authorization.core.repositories.UserRepository;
import org.nsu.authorization.core.utils.JWTTypes;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.authorization.core.utils.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.nsu.users.entity.User;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Service
@RequiredArgsConstructor
public class EmailVerifierService {
    private final JWTUtil jwtUtil;
    @Autowired
    private VerificationCodeCacheRetrieverService verificationCodeCacheRetrieverService;
    @Autowired
    private VerificationCodeGenerator verificationCodeGenerator;
    private final UserRepository userRepository;
    private final EmailVerificationSenderService emailVerificationSenderService;
    private final String tempCodeKeyFirstPart = "registrationService:user:";
    private final String tempCodeKeySecondPart = ":email:code";

    public void verifyEmail(@Valid @RequestBody EmailVerifierRequest dto, String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header. 'Bearer ' prefix missing.");
        }
        String accessToken = authorizationHeader.substring(7);

        String userId;
        try {
            userId = jwtUtil.extractClaim(accessToken, JWTTypes.ACCESS_TOKEN, "userID");

        } catch (JWTVerificationException e) {
            throw new EmailVerificationFailException("Failed to verify email: " + e.getMessage());
        }

        String tempCodeKey = tempCodeKeyFirstPart + userId + tempCodeKeySecondPart;
        String cachedCode = verificationCodeCacheRetrieverService.getCode(tempCodeKey);

        if (cachedCode.isEmpty()) {// if the code is expired, generate a new one and send it
            cachedCode = verificationCodeGenerator.generateVerificationCodeAndCacheIt(tempCodeKey);
            String emailDst;
            try {
                emailDst = jwtUtil.extractClaim(accessToken, JWTTypes.ACCESS_TOKEN, "email");
            } catch (JWTVerificationException e) {
                throw new EmailVerificationFailException("Failed to verify email: " + e.getMessage());
            }
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
