package org.nsu.testutils;

import org.nsu.authorization.core.services.JWTService;
import org.nsu.users.core.repositories.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Primary
@Profile("test")
public class JwtServiceTestImpl extends JWTService {

    public JwtServiceTestImpl(Environment environment, UserRepository userRepository) {
        super(userRepository);
        initializeAlgorithms(
                Objects.requireNonNull(environment.getProperty("jwt.secret-access")).toCharArray(),
                Objects.requireNonNull(environment.getProperty("jwt.secret-refresh")).toCharArray());
    }
}
