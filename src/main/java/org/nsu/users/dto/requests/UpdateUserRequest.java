package org.nsu.users.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

import org.nsu.users.utils.ValidationPatterns;

@Getter
@Setter
@Schema(description = "Запрос на обновление профиля пользователя")
public class UpdateUserRequest {

    @NotBlank
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @NotBlank
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String secondName;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    private String lastName;

    @NotNull
    @Schema(description = "Пол (M/F)", example = "M")
    private GenderRequest gender;

    @NotNull
    @Schema(description = "ID региона", example = "12345")
    private Long locationId;

    @Valid
    @Size(max = 4, message = "Maximum 4 bond time intervals allowed")
    @Schema(description = "Время доступности для связи" )
    private List<BondTimeDto> bondTime;

    @Valid
    @Size(max = 10, message = "Maximum 10 contact methods allowed")
    @Schema(description = "Контактная информация")
    private List<ContactInfoDto> contactInfo;

    @Getter
    @Setter
    @Schema(description = "Время доступности")
    public static class BondTimeDto {
        @NotNull
        @Schema(description = "Время начала доступности", example = "10:00", type = "string", pattern = ValidationPatterns.TIME_FORMAT)
        private LocalTime bondTimeStart;
        
        @NotNull
        @Schema(description = "Время конца доступности", example = "12:00", type = "string", pattern = ValidationPatterns.TIME_FORMAT)
        private LocalTime bondTimeEnd;
    }

    @Getter
    @Setter
    @Schema(description = "Контактная информация")
    public static class ContactInfoDto {
        @NotBlank
        @Schema(description = "Тип контакта", example = "VK")
        private String type;
        
        @NotBlank
        @Schema(description = "Ссылка, номер или email", example = "https://vk.com/user")
        private String contact;
        
        @NotNull
        @Schema(description = "Флаг отображения контакта", example = "true")
        private Boolean visible;
    }
}