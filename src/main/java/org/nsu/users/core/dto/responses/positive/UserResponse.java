package org.nsu.users.core.dto.responses.positive;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Ответ с данными профиля пользователя")
public class UserResponse {

    @Nonnull
    @Schema(description = "Имя пользователя", example = "Тестовик")
    private final String firstName;

    @Nonnull
    @Schema(description = "Отчество пользователя", example = "Тестович")
    private final String secondName;

    @Nullable
    @Schema(description = "Фамилия пользователя", example = "Тестер")
    private final String lastName;

    @Nonnull
    @Schema(description = "Email пользователя", example = "test@mail.ru")
    private final String email;

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
    @Schema(description = "Статус профиля", example = "OK", allowableValues = {"ON_CHECKING", "OK", "BLOCKED"})
    private final String reviewStatus;

    @Nullable
    @Schema(description = "Комментарий администратора")
    private final String reviewComment;

    @Nonnull
    @Schema(description = "Время доступности для связи")
    private final List<BondTimeDto> bondTime;

    @Nonnull
    @Schema(description = "Контактная информация")
    private final List<ContactInfoDto> contactInfo;

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "Время доступности для связи")
    public static class BondTimeDto {
        
        @Nonnull
        @JsonProperty("bondTimeStart")
        @Schema(description = "Время начала доступности", example = "10:00")
        private final OffsetDateTime bondTimeStart;

        @Nonnull
        @JsonProperty("bondTimeEnd")
        @Schema(description = "Время конца доступности", example = "12:00")
        private final OffsetDateTime bondTimeEnd;
    }

    @Getter
    @Setter
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
