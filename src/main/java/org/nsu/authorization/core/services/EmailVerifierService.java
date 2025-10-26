package org.nsu.authorization.core.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.EmailVerifierRequest;
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

    public void verifyEmail(@Valid @RequestBody EmailVerifierRequest dto, String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header. 'Bearer ' prefix missing.");
        }
        String accessToken = authorizationHeader.substring(7);

        String userId;
        try {
            userId = jwtUtil.extractClaim(accessToken, JWTTypes.ACCESS_TOKEN, "userID");

        } catch (JWTVerificationException e) {
            throw new SecurityException("Invalid token: " + e.getMessage(), e);
        }

        String tempCodeKey = "registrationService:user:" + userId + ":email:code";
        String cachedCode = verificationCodeCacheRetrieverService.getCode(tempCodeKey);
        if (cachedCode.isEmpty()) {
            cachedCode = verificationCodeGenerator.generateVerificationCodeAndCacheIt(tempCodeKey);
            emailVerificationSenderService.Send(accessToken, tempCodeKey, cachedCode);
        }

        Long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new SecurityException("Verification code must contain only digits.");
        }
        if (cachedCode.equals(dto.getCode())) {
            User user = userRepository.getReferenceById(id);
            user.setEmailVerified(true);
            userRepository.save(user);
            return;
        }
        throw new SecurityException("Invalid verification code.");
    }

}
