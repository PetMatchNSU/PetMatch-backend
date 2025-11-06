package org.nsu.authorization.core.utils;

import lombok.Getter;

public class JwtClaimKey {
    @Getter
    private static final String USER_ID = "userID";
    @Getter
    private static final String USER_EMAIL = "email";
}
