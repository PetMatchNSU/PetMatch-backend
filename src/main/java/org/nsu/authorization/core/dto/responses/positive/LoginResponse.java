package org.nsu.authorization.core.dto.responses.positive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

    @NotBlank
    @Schema(description = "Access JWT токен для авторизации на сервисах", example = "some.access.token")
    private String accessToken;

    @NotBlank
    @Schema(description = "Refresh JWT токен для восстановления Access и Refresh JWT токенов", example = "some.refresh.token")
    private String refreshToken;

    @NonNull
    private Map<String, Object> user;
}
