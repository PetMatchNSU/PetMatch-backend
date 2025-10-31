package org.nsu.authorization.core.dto.requests.registrationRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Способ связи")
public class ContactInfo {
    @NotBlank(message = "Contact type should not be empty")
    @Schema(description = "Тип связи (PHONE/EMAIL/TELEGRAM/VK)", example = "VK")
    private String type;

    @NotBlank(message = "Contact detail should not be empty")
    @Schema(description = "Ссылка, номер или email", example = "https://vk.com/t.test")
    private String contact;

    @NotNull(message = "Visibility flag should not be null")
    @Schema(description = "Флаг отображения конкретного типа связи", example = "true")
    private Boolean visible;
}
