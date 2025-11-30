package org.nsu.users.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.nsu.animal.entity.AnimalGender;
import org.nsu.users.dto.responses.util.ReviewStatus;
import org.nsu.users.dto.responses.util.Goal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response: список животных пользователя")
public class UserAnimalListResponse {
    @Schema(description = "массив с информацией о питомцах")
    private List<Animal> animalsList;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Информация о питомце")
    public static class Animal {
        @Schema(description = "идентификатор питомца", example = "1")
        private Long id;

        @Schema(description = "кличка животного", example = "Ху")
        private String name;

        @Schema(description = "текстовое описание вида животного", example = "Кошка")
        private String speciesName;

        @Schema(description = "цель размещения объявления", example = "SELL")
        private Goal goal;

        @Schema(description = "текстовое название породы (если есть)", example = "Абиссинская")
        private String breed;

        @Schema(description = "гендер животного М/F", example = "M")
        private AnimalGender gender;

        @Schema(description = "дата рождения животного в формате '2023-11-15' ГГГГ-ММ-ДД", example = "2023-11-15")
        private LocalDate birthday;

        @Schema(description = "идентификатор главного фото", example = "1324325")
        private Long mainPhotoId;

        @Schema(description = "статус объявления, возможные варианты ON_CHECKING/OK/BLOCKED", example = "BLOCKED")
        private ReviewStatus reviewStatus;

        @Schema(description = "комментарий от проверяющего админа", example = "Некорректная главная фотография")
        private String reviewComment;
    }
}
