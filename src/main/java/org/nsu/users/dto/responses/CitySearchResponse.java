package org.nsu.users.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CitySearchResponse {

    private List<LocationDto> locations;

    @Getter
    @AllArgsConstructor
    public static class LocationDto {
        private Long locationId;
        private String region;
        private String city;
    }
}