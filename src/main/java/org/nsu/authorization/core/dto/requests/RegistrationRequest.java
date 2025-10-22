package org.nsu.authorization.core.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class RegistrationRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email should not be empty")
    @Schema(description = "Почта пользователя", example = "i.ivanov@ivanov.ru")
    private String email;

    @NotBlank(message = "Password should not be empty")
    @Schema(description = "Пароль пользователя", example = "09090909")
    @Length(min = 8, max = 64)
    private String password;

    @NotBlank(message = "First name should not be empty")
    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @NotBlank(message = "Second name should not be empty")
    @Schema(description = "Фамилия", example = "Иванов")
    private String secondName;

    @Schema(description = "Отчество", example = "Иванович")
    private String lastName;

    @NotBlank(message = "Gender should not be empty")
    @Schema(description = "Пол (М/Ж)", example = "M")
    private String gender;

    @NotBlank(message = "Region should not be empty")
    @Schema(description = "Регион проживания пользователя", example = "Новосибирская область")
    private String region;

    @NotBlank(message = "City should not be empty")
    @Schema(description = "Город проживания пользователя", example = "Новосибирск")
    private String city;

    @NotNull(message = "Bond time array must be provided")
    @Size(min = 1, message = "At least one bond time interval is required")
    @Valid
    @Schema(description = "Массив времени для связи")
    private List<BondTime> bondTime;

    @NotNull(message = "Contact info array must be provided")
    @Size(min = 1, message = "At least one contact method is required")
    @Valid
    @Schema(description = "Массив способов связи")
    private List<ContactInfo> contactInfo;

    @Getter
    @Setter
    @Schema(description = "Временной интервал для связи")
    private static class BondTime {
        @NotBlank(message = "Bond time start should not be empty")
        @Schema(description = "Начало временного интервала (HH:MM)", example = "10:00")
        private String bondTimeStart;

        @NotBlank(message = "Bond time end should not be empty")
        @Schema(description = "Конец временного интервала (HH:MM)", example = "12:00")
        private String bondTimeEnd;
    }

    @Getter
    @Setter
    @Schema(description = "Способ связи")
    private static class ContactInfo {
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
}
