package org.nsu.authorization.core.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.LoginRequest;
import org.nsu.authorization.core.dto.responses.positive.LoginResponse;
import org.nsu.authorization.core.exceptions.authorization.PersonHasNotVerifiedEmailException;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.authorization.core.utils.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final PersonDetailsService personDetailsService;
    private final JWTUtil jwtUtil;

    public LoginResponse login(@Valid @RequestBody LoginRequest dto) {

        PersonDetails personDetails = personDetailsService.loadUserByUsername(dto.getEmail());

        if (!personDetails.getIsVerifiedEmail()) throw new PersonHasNotVerifiedEmailException("Email is not verified");

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(personDetails.getUsername(), personDetails.getPassword());

        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            throw new PersonNotFoundException("Email or password is incorrect");
        }

        String accessToken = jwtUtil.generateAccessToken(authenticationToken);
        String refreshToken = jwtUtil.generateRefreshToken(authenticationToken);

        return new LoginResponse(
                accessToken,
                refreshToken,
                personDetails.getIsVerifiedEmail()
        );

    }
}
