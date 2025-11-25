package org.nsu.feed.dto.responses;

import java.util.List;

import org.nsu.feed.dto.requests.animalList.Filter.Species;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimalInfoResponse {
    @Schema(description = "Массив с видами животных")
    private List<Species> species;

    @Schema(description = "Массив с целями размещения")
    private List<Goal> goals;

    @Getter
    @Setter
    public static class Goal {
        @Schema(description = "Идентификатор цели размещения", example = "1")
        private Long id;

        @Schema(description = "Название цели размещения", example = "SELL")
        private String name;
    }
}
