package org.nsu.authorization.core.services;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.responses.positive.RefreshResponse;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.utils.JWTTypes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final JWTService jwtService;

    public RefreshResponse refreshTokens(String refreshToken) {

        String email;

        try {
            email = jwtService.extractClaim(refreshToken, JWTTypes.REFRESH_TOKEN, "email");
        } catch (JWTVerificationException e) {
            throw new JWTIsExpiredException(JWTTypes.REFRESH_TOKEN);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, null);

        String newAccessToken = jwtService.generateAccessToken(authenticationToken);
        String newRefreshToken = jwtService.generateRefreshToken(authenticationToken);

        return new RefreshResponse(newAccessToken, newRefreshToken);

    }
}
