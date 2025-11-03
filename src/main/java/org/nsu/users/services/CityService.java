package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.dto.responses.CitySearchResponse;
import org.nsu.users.entity.Region;
import org.nsu.users.mappers.RegionMapper;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CityService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    public CitySearchResponse searchCities(String name) {
        return Optional.ofNullable(name)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(regionRepository::searchByRegionOrCity)
                .map(regionMapper::toLocationDtoList)
                .map(CitySearchResponse::new)
                .orElse(CitySearchResponse.EMPTY);
    }
}