package org.nsu.authorization.core.dto.requests.registrationRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.hibernate.validator.constraints.Length;
import org.nsu.users.entity.Gender;
import org.nsu.users.utils.ValidationPatterns;

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
    @Pattern(regexp = ValidationPatterns.NAME_REQUIRED, message = "First name can only contain letters, spaces, hyphens and apostrophes")
    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @NotBlank(message = "Second name should not be empty")
    @Pattern(regexp = ValidationPatterns.NAME_REQUIRED, message = "Second name can only contain letters, spaces, hyphens and apostrophes")
    @Schema(description = "Фамилия", example = "Иванов")
    private String secondName;

    @Pattern(regexp = ValidationPatterns.NAME_OPTIONAL, message = "Last name can only contain letters, spaces, hyphens and apostrophes")
    @Schema(description = "Отчество", example = "Иванович")
    private String lastName;

    @NotNull(message = "Gender should not be empty")
    @Schema(description = "Пол (M/F)", example = "M")
    private Gender gender;

    @NotBlank(message = "Region should not be empty")
    @Schema(description = "Регион проживания пользователя", example = "Новосибирская область")
    private String region;

    @NotBlank(message = "City should not be empty")
    @Schema(description = "Город проживания пользователя", example = "Новосибирск")
    private String city;

    @NotNull(message = "Bond time array must be provided")
    @Size(min = 1, max = 4, message = "Bond time intervals must be between 1 and 4")
    @Valid
    @Schema(description = "Массив времени для связи")
    private List<BondTime> bondTime;

    @NotNull(message = "Contact info array must be provided")
    @Size(min = 1, max = 10, message = "Contact info must be between 1 and 10")
    @Valid
    @Schema(description = "Массив способов связи")
    private List<ContactInfo> contactInfo;
}
