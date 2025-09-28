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

        long id;

        try {
            id = Long.parseLong(jwtUtil.extractClaim(refreshToken, JWTTypes.refreshToken, "id"));
        } catch (JWTVerificationException e) {
            throw new JWTIsExpiredException(JWTTypes.refreshToken);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(id, null);

        String newAccessToken = jwtUtil.generateAccessToken(authenticationToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(authenticationToken);

        return new RefreshResponse(newAccessToken, newRefreshToken);

    }
}
