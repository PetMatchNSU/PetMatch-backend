package org.nsu.users.controllers;

import lombok.RequiredArgsConstructor;
import org.nsu.users.dto.responses.CitySearchResponse;
import org.nsu.users.entity.Region;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CityController {

    private final RegionRepository regionRepository;

    @GetMapping("/city")
    public ResponseEntity<CitySearchResponse> search(@RequestParam("name") String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.ok(new CitySearchResponse(List.of()));
        }
        List<Region> regions = regionRepository.searchByRegionOrCity(name.trim());
        List<CitySearchResponse.LocationDto> items = regions.stream()
                .map(r -> new CitySearchResponse.LocationDto(r.getId(), r.getRegion(), r.getCity()))
                .toList();

        return ResponseEntity.ok(new CitySearchResponse(items));
    }
}