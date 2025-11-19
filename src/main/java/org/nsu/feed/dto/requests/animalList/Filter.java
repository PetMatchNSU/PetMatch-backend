package org.nsu.feed.dto.requests.animalList;

import java.util.List;

import org.nsu.animal.entity.AnimalGender;
import org.nsu.feed.dto.util.Location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter {
    @Schema(description = "Идентификатор пользователя", example = "12345")
    private Long userId;

    @Schema(description = "Вид животного")
    private Species species;

    @Schema(description = "породистость животного, возможные варианты true/false (если не пришёл - не важно, true - да, false - нет)", example = "true")
    private Boolean hadBreed;

    @Schema(description = "массив с названиями пород и их идентификаторами")
    private List<Breed> breeds;

    @Schema(description = "гендер, возможные варианты M/F (если не пришёл - не важно, M - мужской, F - женский)", example = "M")
    private AnimalGender gender;

    @Schema(description = "массив с целью размещения объявления", example = "SELL")
    private List<String> goals;

    @Schema(description = "флаг наличия ветеринарного паспорта, возможные варианты true/false (если не пришёл - не важно, true - да, false - нет)", example = "true")
    private Boolean vetPassport;

    @Schema(description = "флаг наличия метрики/родословной, возможные варианты true/false (если не пришёл - не важно, true - да, false - нет)", example = "false")
    private Boolean pedigree;

    @Schema(description = "Информация о регионе и городе")
    private Location location;

    @Getter
    @Setter
    public static class Species {
        @Schema(description = "идентификатор вида животного", example = "2")
        private Long id;

        @Schema(description = "текстовое описание вида животного", example = "Собака")
        private String name;
    }

    @Getter
    @Setter
    public static class Breed {
        @Schema(description = "идентификатор породы", example = "15")
        private Long idBreed;

        @Schema(description = "название породы", example = "Лабрадор")
        private String breedName;
    }
}
