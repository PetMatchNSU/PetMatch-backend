package org.nsu.users.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Ответ с данными профиля пользователя")
public class UserProfileResponse {

    @Nonnull
    @Schema(description = "Полное ФИО пользователя", example = "Иванов Иван Иванович")
    private final String fullName;

    @Nonnull
    @Schema(description = "Пол пользователя", example = "M", allowableValues = {"M", "F"})
    private final String gender;

    @Nonnull
    @Schema(description = "Название региона", example = "Новосибирская область")
    private final String region;

    @Nonnull
    @Schema(description = "Название города", example = "Новосибирск")
    private final String city;

    @Nonnull
    @Schema(description = "Время доступности для связи")
    private final List<BondTimeDto> bondTime;

    @Nonnull
    @Schema(description = "Контактная информация")
    private final List<ContactInfoDto> contactInfo;

    @Nonnull
    @Schema(description = "Статус профиля", example = "OK", allowableValues = {"ON_CHECKING", "OK", "BLOCKED"})
    private final String reviewStatus;

    @Nullable
    @Schema(description = "Комментарий администратора")
    private final String reviewComment;

    @Getter
    @AllArgsConstructor
    @Schema(description = "Время доступности для связи")
    public static class BondTimeDto {
        
        @Nonnull
        @JsonProperty("bondTimeStart")
        @Schema(description = "Время начала доступности", example = "10:00")
        private final LocalTime bondTimeStart;

        @Nonnull
        @JsonProperty("bondTimeEnd")
        @Schema(description = "Время конца доступности", example = "12:00")
        private final LocalTime bondTimeEnd;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "Контактная информация")
    public static class ContactInfoDto {
        
        @Nonnull
        @Schema(description = "Тип контакта", example = "VK")
        private final String type;

        @Nonnull
        @Schema(description = "Ссылка, номер или email", example = "https://vk.com/t.test")
        private final String contact;

        @Schema(description = "Флаг отображения конкретного типа связи", example = "true")
        private final boolean visible;
    }
}
