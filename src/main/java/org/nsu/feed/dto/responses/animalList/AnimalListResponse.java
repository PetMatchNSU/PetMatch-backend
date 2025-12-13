package org.nsu.feed.dto.responses.animalList;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimalListResponse {
    @Schema(description = "массив с информацией о питомцах")
    private List<AnimalCardDto> animalsList;

    @Schema(description = "Информация о страницах?")
    private Pagination pagination;

    @Getter
    @Setter
    public static class Pagination {
        @Schema(description = "Текущая страница", example = "1")
        private Long currentPage;

        @Schema(description = "Size of the page", example = "20")
        private Long pageSize;

        @Schema(description = "Total number of pages", example = "15")
        private Long totalPages;

        @Schema(description = "Total number of items", example = "287")
        private Long totalItems;

        @Schema(description = "Flag that states whether there is a next page", example = "true")
        private boolean hasNextPage;

        @Schema(description = "Flag that states whether there is a previous page", example = "false")
        private boolean hasPreviousPage;
    }
}
