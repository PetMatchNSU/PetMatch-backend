package org.nsu.authorization.core.dto.requests.registrationRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Schema(description = "Временной интервал для связи")
public class BondTime {
    @NotBlank(message = "Bond time start should not be empty")
    @Schema(description = "Начало временного интервала (HH:MM)", example = "10:00")
    private String bondTimeStart;

    @NotBlank(message = "Bond time end should not be empty")
    @Schema(description = "Конец временного интервала (HH:MM)", example = "12:00")
    private String bondTimeEnd;
}
