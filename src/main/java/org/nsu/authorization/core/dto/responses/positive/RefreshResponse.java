package org.nsu.authorization.core.dto.responses.positive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class RefreshResponse {

    private String accessToken;

    private String refreshToken;

}
