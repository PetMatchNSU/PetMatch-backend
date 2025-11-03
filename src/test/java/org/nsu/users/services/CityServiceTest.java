package org.nsu.users.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.users.dto.responses.CitySearchResponse;
import org.nsu.users.entity.Region;
import org.nsu.users.mappers.RegionMapper;
import org.nsu.users.repositories.RegionRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionMapper regionMapper;

    @InjectMocks
    private CityService cityService;

    @Test
    void shouldReturnEmptyWhenNameIsNull() {
        CitySearchResponse resp = cityService.searchCities(null);
        assertEquals(CitySearchResponse.EMPTY, resp);

        resp = cityService.searchCities("");
        assertEquals(CitySearchResponse.EMPTY, resp);
    }

    @Test
    void shouldMapRegionsToResponse() {
        Region r1 = new Region(1L, "Reg1", "City1");
        Region r2 = new Region(2L, "Reg2", "City2");
        List<Region> regions = List.of(r1, r2);

        var dto1 = new CitySearchResponse.LocationDto(1L, "Reg1", "City1");
        var dto2 = new CitySearchResponse.LocationDto(2L, "Reg2", "City2");

        when(regionRepository.searchByRegionOrCity(anyString())).thenReturn(regions);
        when(regionMapper.toLocationDtoList(regions)).thenReturn(List.of(dto1, dto2));

        CitySearchResponse resp = cityService.searchCities("test");
        assertEquals(2, resp.getLocations().size());
        assertEquals(dto1.getLocationId(), resp.getLocations().get(0).getLocationId());
    }
}
