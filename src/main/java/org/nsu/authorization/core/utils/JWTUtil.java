package org.nsu.authorization.core.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.repositories.UserRepository;
import org.nsu.users.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt_secret_access}")
    private String JWTAccessSecret;

    @Value("${jwt_secret_refresh}")
    private String JWTRefreshSecret;

    private UserRepository userRepository;

    private String generateJWT(User user, Date expirationDate, String JWTToken) {

        return JWT.create()
                .withSubject("Person details")
                .withClaim("userID", user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("firstName", user.getFirstName())
                .withClaim("surname", user.getSecondName())
                .withClaim("patronymic", user.getLastName())
                .withClaim("authorities", user.getAuthorities().stream().toList())
                .withIssuer("spring-app")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(JWTToken));
    }

    public String generateAccessToken(Authentication authentication) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusHours(1).toInstant());

        // тк в аутентификации поменять поле нельзя, то в поле Name будет лежать Id. Всё равно это нужно будет только в сервисе аутентификации
        User user = userRepository.findById(Long.parseLong(authentication.getName())).orElseThrow(() -> new PersonNotFoundException(String.format("Person with id %s not found", authentication.getName())));

        return generateJWT(user, expirationDate, JWTAccessSecret);
    }

    public String generateRefreshToken(Authentication authentication) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusDays(7).toInstant());

        User user = userRepository.findById(Long.parseLong(authentication.getName())).orElseThrow(() -> new PersonNotFoundException(String.format("Person with id %s not found", authentication.getName())));

        return generateJWT(user, expirationDate, JWTRefreshSecret);
    }

    public DecodedJWT verifyJWT(String token, JWTTypes jwtType) {

        String secret = switch (jwtType) {
            case JWTTypes.accessToken -> JWTAccessSecret;
            case JWTTypes.refreshToken -> JWTRefreshSecret;
        };

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("Person details")
                .withIssuer("spring-app")
                .build();

        return verifier.verify(token); // тут происходит валидность JWT токена
    }

    public String extractClaim(String token, JWTTypes jwtType, String claim) {

        DecodedJWT jwt = verifyJWT(token, jwtType);
        return jwt.getClaim(claim).asString();
    }


}
