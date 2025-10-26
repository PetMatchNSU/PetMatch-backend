package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.dto.responses.CitySearchResponse;
import org.nsu.users.entity.Region;
import org.nsu.users.mappers.RegionMapper;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    public CitySearchResponse searchCities(String name) {
        if (name == null || name.isBlank()) {
            return new CitySearchResponse(List.of());
        }
        
        List<Region> regions = regionRepository.searchByRegionOrCity(name.trim());
        List<CitySearchResponse.LocationDto> locations = regionMapper.toLocationDtoList(regions);
        
        return new CitySearchResponse(locations);
    }
}