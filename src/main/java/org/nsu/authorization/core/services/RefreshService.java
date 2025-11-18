package org.nsu.authorization.core.services;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.responses.positive.RefreshResponse;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.utils.JWTTypes;
import org.nsu.authorization.core.utils.JWTUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final JWTUtil jwtUtil;

    public RefreshResponse refreshTokens(String refreshToken) {

        String email;

        try {
            email = jwtUtil.extractClaim(refreshToken, JWTTypes.REFRESH_TOKEN, "email");
        } catch (JWTVerificationException e) {
            throw new JWTIsExpiredException(JWTTypes.REFRESH_TOKEN);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, null);

        String newAccessToken = jwtUtil.generateAccessToken(authenticationToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(authenticationToken);

        return new RefreshResponse(newAccessToken, newRefreshToken);

    }
}
