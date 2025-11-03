package org.nsu.users.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "Результат поиска локаций (регион/город)")
public class CitySearchResponse {

    @Schema(description = "Список локаций")
    private List<LocationDto> locations;

    public static final CitySearchResponse EMPTY = new CitySearchResponse(List.of());

    public static CitySearchResponse empty() {
        return EMPTY;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "Локация: регион и населённый пункт")
    public static class LocationDto {
        @Schema(description = "Id локации")
        private Long locationId;
        @Schema(description = "Регион")
        private String region;
        @Schema(description = "Населённый пункт")
        private String city;
    }
}