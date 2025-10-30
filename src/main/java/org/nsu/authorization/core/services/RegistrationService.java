package org.nsu.authorization.core.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.RegistrationRequest;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.authorization.core.utils.VerificationCodeGenerator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.nsu.users.entity.User;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final PersonDetailsService personDetailsService;
    private final EmailVerificationSenderService emailVerificationSenderService;
    private final JWTUtil jwtUtil;
    private VerificationCodeGenerator verificationCodeGenerator;
    private final String emailSubject = "Email Verification";

    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest dto) {

        try {
            personDetailsService.loadUserByUsername(dto.getEmail());
            throw new UserAlreadyExistsException("A user with this email already exists.");
        } catch (UsernameNotFoundException e) {
        }

        User user = userService.AddNewUser(dto);

        String tempCodeKey = "registrationService:user:" + user.getId() + ":email:code";
        String tempCode = verificationCodeGenerator.generateVerificationCodeAndCacheIt(tempCodeKey);
        String emailText = "Регистрация на платформе Pet Match почти закончена! Пожалуйста, подтвердите свою электронную почту, введя следующий временный код на последнем шаге регистрации: "
                + tempCode + ". Спасибо, что Вы с нами! Ваш Pet Match.";

        emailVerificationSenderService.Send(dto.getEmail(), emailSubject, emailText);

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
