package org.nsu.feed.dto.requests.animalList;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimalListRequest {

    @NotNull
    @Schema(description = "Информация о страницах")
    private Pagination pagination;

    @Getter
    @Setter
    public static class Pagination {
        @Schema(description = "Текущая страница?", example = "1")
        private Long page;

        @Schema(description = "Лимит страница для загрузки?")
        private Long limit;
    }
}
