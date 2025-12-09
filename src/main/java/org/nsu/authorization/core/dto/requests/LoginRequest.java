package org.nsu.authorization.core.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email should not be empty")
    @Schema(description = "Почта пользователя", example = "a.kardash@g.nsu.ru")
    private String email;

    @NotBlank(message = "Password should not be empty")
    @Schema(description = "Пароль пользователя", example = "1234567890")
    @Length(min = 8, max = 64)
    private String password;

}
