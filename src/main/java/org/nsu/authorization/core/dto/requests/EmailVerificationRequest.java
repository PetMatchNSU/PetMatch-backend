package org.nsu.authorization.core.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequest {
    @NotBlank(message = "Code should not be empty")
    @Schema(description = "Код подтверждения", example = "eqtr6q798wet6")
    private String code;

}
