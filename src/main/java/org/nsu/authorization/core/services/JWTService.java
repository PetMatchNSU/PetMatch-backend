package org.nsu.authorization.core.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.utils.JWTTypes;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.Authority;
import org.nsu.users.entity.User;
import org.springframework.security.core.Authentication;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public abstract class JWTService {

	private final UserRepository userRepository;

	private Algorithm accessAlgorithm;
	private Algorithm refreshAlgorithm;

	protected JWTService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	protected void initializeAlgorithms(char[] accessChars, char[] refreshChars) {
		byte[] accessKey = null;
		byte[] refreshKey = null;

		try {
			accessKey = decodeSecret(accessChars);
			refreshKey = decodeSecret(refreshChars);

			this.accessAlgorithm = Algorithm.HMAC256(accessKey);
			this.refreshAlgorithm = Algorithm.HMAC256(refreshKey);
		} finally {
			if (accessKey != null)
				Arrays.fill(accessKey, (byte) 0);
			if (refreshKey != null)
				Arrays.fill(refreshKey, (byte) 0);
			Arrays.fill(accessChars, '\0');
			Arrays.fill(refreshChars, '\0');
		}
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

	private byte[] decodeSecret(char[] encoded) {
		if (isLikelyHex(encoded)) {
			return hexToBytes(encoded);
		}
		// treat as Base64 (URL-safe or standard)
		byte[] ascii = charsToAsciiBytes(encoded);
		try {
			try {
				return Base64.getUrlDecoder().decode(ascii);
			} catch (IllegalArgumentException ignored) {
				return Base64.getDecoder().decode(ascii);
			}
		} finally {
			Arrays.fill(ascii, (byte) 0);
		}
	}

	private boolean isLikelyHex(char[] encoded) {
		if ((encoded.length & 1) != 0)
			return false;
		for (char c : encoded) {
			boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
			if (!isHex)
				return false;
		}
		return true;
	}

	private byte[] hexToBytes(char[] hex) {
		int len = hex.length;
		byte[] out = new byte[len / 2];
		for (int i = 0, j = 0; i < len; i += 2, j++) {
			int hi = Character.digit(hex[i], 16);
			int lo = Character.digit(hex[i + 1], 16);
			out[j] = (byte) ((hi << 4) + lo);
		}
		return out;
	}

	private byte[] charsToAsciiBytes(char[] chars) {
		CharBuffer cb = CharBuffer.wrap(chars);
		ByteBuffer bb = StandardCharsets.US_ASCII.encode(cb);
		byte[] arr = new byte[bb.remaining()];
		bb.get(arr);
		return arr;
	}

	private String generateJWT(User user, Date expirationDate, Algorithm algorithm) {

		return JWT.create()
				.withSubject("Person details")
				.withClaim("userID", user.getId().toString())
				.withClaim("email", user.getEmail())
				.withClaim("firstName", user.getFirstName())
				.withClaim("surname", user.getSecondName())
				.withClaim("patronymic", user.getLastName())
				.withClaim("authorities",
						user.getAuthorities().stream().map(Authority::getAuthority).toList())
				.withIssuer("spring-app")
				.withExpiresAt(expirationDate)
				.sign(algorithm);
	}

	public String generateAccessToken(Authentication authentication) {
		Date expirationDate = Date.from(ZonedDateTime.now().plusHours(1).toInstant());

		User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new PersonNotFoundException(
				String.format("Person with email %s not found", authentication.getName())));

		return generateJWT(user, expirationDate, accessAlgorithm);
	}

	public String generateRefreshToken(Authentication authentication) {
		Date expirationDate = Date.from(ZonedDateTime.now().plusDays(7).toInstant());

		User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new PersonNotFoundException(
				String.format("Person with email %s not found", authentication.getName())));

		return generateJWT(user, expirationDate, refreshAlgorithm);
	}

	public DecodedJWT verifyJWT(String token, JWTTypes jwtType) {

		Algorithm algorithm = switch (jwtType) {
			case JWTTypes.ACCESS_TOKEN -> accessAlgorithm;
			case JWTTypes.REFRESH_TOKEN -> refreshAlgorithm;
		};

		JWTVerifier verifier = JWT.require(algorithm)
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
