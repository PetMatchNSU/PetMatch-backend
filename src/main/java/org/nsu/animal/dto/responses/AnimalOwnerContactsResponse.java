package org.nsu.animal.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с контактной информацией владельца питомца")
public class AnimalOwnerContactsResponse {

    @Schema(description = "Имя владельца", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия владельца", example = "Иванов")
    private String secondName;

    @Schema(description = "Отчество владельца", example = "Владимирович")
    private String middleName;

    @Schema(description = "Временные интервалы для связи")
    private List<BondTimeDto> bondTime;

    @Schema(description = "Контактная информация")
    private List<ContactInfoDto> contactInfo;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Временной интервал для связи")
    public static class BondTimeDto {
        @Schema(description = "Начало временного интервала", example = "10:00")
        private LocalTime bondTimeStart;

        @Schema(description = "Конец временного интервала", example = "12:00")
        private LocalTime bondTimeEnd;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Контактная информация")
    public static class ContactInfoDto {
        @Schema(description = "Тип связи", example = "VK")
        private String type;

        @Schema(description = "Ссылка, номер или email", example = "https://vk.com/t.test")
        private String contact;
    }
}
