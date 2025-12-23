package org.nsu.feed.dto.responses.animalList;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.nsu.animal.entity.AnimalGender;
import org.nsu.feed.dto.util.Location;

@Getter
@Setter
public class AnimalCardDto {
    @Schema(description = "идентификатор питомца", example = "1111")
    private Long animalId;

    @Schema(description = "кличка животного", example = "Кличка")
    private String name;

    @Schema(description = "текстовое описание вида животного", example = "Кошка")
    private String speciesName;

    @Schema(description = "цель размещения объявления", example = "BUY")
    private String goal;

    @Schema(description = "породистость животного", example = "true")
    private boolean hasBreed;

    @Schema(description = "текстовое название породы", example = "Абиссинская")
    private String breed;

    @Schema(description = "гендер животного М/F", example = "M")
    private AnimalGender gender;

    @Schema(description = "дата рождения животного в формате '2023-11-15' ГГГГ-ММ-ДД", example = "2023-11-15")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Schema(description = "Информация о регионе и городе")
    private Location location;

    @Schema(description = "идентификатор главного фото", example = "1324325")
    private Long mainPhotoId;

    @Schema(description = "дата и время создания карточки", example = "2023-11-15T12:00:00Z", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}
