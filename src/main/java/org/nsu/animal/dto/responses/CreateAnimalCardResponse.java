package org.nsu.animal.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Ответ при создании карточки животного")
public class CreateAnimalCardResponse {

    @Nonnull
    @Schema(description = "Идентификатор созданной карточки животного", example = "1245")
    private Long animalId;
}
