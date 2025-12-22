package org.nsu.animal.dto.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.nsu.animal.dto.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Запрос на обновление карточки животного")
public class UpdateAnimalCardRequest {

    @NotBlank(message = "Кличка не может быть пустой")
    @Length(min = 1, max = 64, message = "Кличка должна содержать от 1 до 64 символов")
    @Pattern(regexp = "^[а-яА-ЯёЁa-zA-Z0-9\\s\\-/]+$", message = "Кличка может содержать только русские и английские буквы, цифры, пробелы, символы '-' и '/'")
    @Schema(description = "Кличка животного", example = "Барсик", requiredMode = RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Вид животного обязателен для заполнения")
    @Schema(description = "Идентификатор вида животного", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Long speciesId;

    @NotNull(message = "Цель размещения обязательна для заполнения")
    @Pattern(regexp = "^(SELL|PAIRING|FREE)$", message = "Цель размещения должна быть одной из: SELL, PAIRING, FREE")
    @Schema(description = "Цель размещения объявления", example = "SELL", allowableValues = {"SELL", "PAIRING", "FREE"}, requiredMode = RequiredMode.REQUIRED)
    private String goal;

    @DecimalMin(value = "0", message = "Стоимость не может быть отрицательной")
    @DecimalMax(value = "1000000", message = "Стоимость не может превышать 1 000 000")
    @Schema(description = "Стоимость продажи (обязательно если цель - продажа)", example = "15000")
    private BigDecimal cost;

    @NotNull(message = "Признак породистого животного обязателен для заполнения")
    @Schema(description = "Признак породистого животного", example = "true", requiredMode = RequiredMode.REQUIRED)
    private Boolean hasBreed;

    @Length(min = 3, max = 64, message = "Порода должна содержать от 3 до 64 символов")
    @Pattern(regexp = "^[а-яА-ЯёЁa-zA-Z\\s\\-/]+$", message = "Порода может содержать только русские и английские буквы, пробелы, символы '-' и '/'")
    @Schema(description = "Название породы (если животное породистое)", example = "Британская короткошерстная", requiredMode = RequiredMode.NOT_REQUIRED)
    private String breed;

    @NotNull(message = "Пол животного обязателен для заполнения")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Пол животного", example = "M", allowableValues = {"M", "F"}, requiredMode = RequiredMode.REQUIRED)
    private Gender gender;

    @NotNull(message = "Дата рождения обязательна для заполнения")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата рождения животного", example = "2023-11-15", requiredMode = RequiredMode.REQUIRED)
    private LocalDate birthday;

    @DecimalMin(value = "0.1", message = "Вес должен быть больше 0")
    @Digits(integer = 6, fraction = 1, message = "Вес может содержать максимум 6 цифр до запятой и 1 после")
    @Schema(description = "Вес животного в кг", example = "5.8")
    private BigDecimal weight;

    @NotBlank(message = "Окрас не может быть пустым")
    @Length(min = 3, max = 200, message = "Окрас должен содержать от 3 до 200 символов")
    @Schema(description = "Описание окраса животного", example = "Серый с белыми пятнами", requiredMode = RequiredMode.REQUIRED)
    private String color;

    @NotBlank(message = "Информация о наследственных заболеваниях не может быть пустой")
    @Length(min = 3, max = 2000, message = "Информация о наследственных заболеваниях должна содержать от 3 до 2000 символов")
    @Schema(description = "Описание наследственных заболеваний", example = "Наследственных заболеваний не выявлено", requiredMode = RequiredMode.REQUIRED)
    private String geneticDiseases;

    @Length(max = 2000, message = "Описание не может содержать более 2000 символов")
    @Schema(description = "Дополнительное описание животного", example = "Очень дружелюбный и игривый кот")
    private String description;
}
