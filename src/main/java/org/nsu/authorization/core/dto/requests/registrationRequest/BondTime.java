package org.nsu.authorization.core.dto.requests.registrationRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

@Getter
@Setter
@Schema(description = "Временной интервал для связи")
public class BondTime {
    @NotNull(message = "Bond time start should not be empty")
    @Schema(description = "Начало временного интервала (HH:MM)", example = "10:00")
    private LocalTime bondTimeStart;

    @NotNull(message = "Bond time end should not be empty")
    @Schema(description = "Конец временного интервала (HH:MM)", example = "12:00")
    private LocalTime bondTimeEnd;
}
