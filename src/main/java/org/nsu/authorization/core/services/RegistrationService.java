package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.nsu.authorization.core.utils.JWTUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.nsu.users.entity.User;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final EmailVerificationSenderService emailVerificationSenderService;
    private final JWTUtil jwtUtil;
    private final VerificationCodeCachingService verificationCodeCachingService;

    public RegistrationResponse register(RegistrationRequest dto) {

        if (userService.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        }

        User user = userService.addNewUser(dto);

        String tempCode = verificationCodeCachingService.generateAndCacheCode(user.getId().toString());

        emailVerificationSenderService.Send(dto.getEmail(), tempCode);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                dto.getEmail(), dto.getPassword());

        String accessToken = jwtUtil.generateAccessToken(authenticationToken);
        String refreshToken = jwtUtil.generateRefreshToken(authenticationToken);

        boolean isEmailVerified = false;
        return new RegistrationResponse(
                accessToken,
                refreshToken,
                new RegistrationResponse.UserDto(isEmailVerified));
    }
}
