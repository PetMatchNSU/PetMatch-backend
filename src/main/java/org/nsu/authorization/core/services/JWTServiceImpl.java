package org.nsu.authorization.core.services;

import org.nsu.users.core.repositories.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JWTServiceImpl extends JWTService {

    protected JWTServiceImpl(UserRepository userRepository) {
        super(userRepository);
        char[] accessChars = readEnvAsChars("JWT_SECRET_ACCESS");
        char[] refreshChars = readEnvAsChars("JWT_SECRET_REFRESH");
        initializeAlgorithms(accessChars, refreshChars);
    }

	private char[] readEnvAsChars(String name) {
		String value = System.getenv(name);
		if (value == null || value.isEmpty()) {
			throw new IllegalStateException("Environment variable '" + name + "' is required");
		}
		char[] chars = new char[value.length()];
		value.getChars(0, value.length(), chars, 0);
		return chars;
	}
}
