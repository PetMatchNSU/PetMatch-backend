package org.nsu.authorization.core.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.LoginRequest;
import org.nsu.authorization.core.dto.responses.positive.LoginResponse;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.security.PersonDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final PersonDetailsService personDetailsService;
    private final JWTService jwtService;

    private static final String INVALID_CREDENTIALS_MESSAGE = "Неверный email или пароль";

    public LoginResponse login(@Valid @RequestBody LoginRequest dto) {

        PersonDetails personDetails;
        try {
            personDetails = personDetailsService.loadUserByUsername(dto.getEmail());
        } catch (PersonNotFoundException | UsernameNotFoundException e) {
            throw new PersonNotFoundException(INVALID_CREDENTIALS_MESSAGE);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(personDetails.getUsername(), dto.getPassword());

        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            throw new PersonNotFoundException(INVALID_CREDENTIALS_MESSAGE);
        }

        String accessToken = jwtService.generateAccessToken(authenticationToken);
        String refreshToken = jwtService.generateRefreshToken(authenticationToken);

        return new LoginResponse(
                accessToken,
                refreshToken,
                new LoginResponse.UserDto(personDetails.getIsVerifiedEmail())
        );
    }
}
