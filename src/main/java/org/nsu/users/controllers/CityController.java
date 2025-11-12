package org.nsu.users.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nsu.users.dto.responses.CitySearchResponse;
import org.nsu.users.services.CityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/city")
@Tag(name = "City", description = "API для работы с городами и регионами")
public class CityController {

    private final CityService cityService;

    @GetMapping
    @Operation(summary = "Поиск городов и регионов", description = "Поиск городов и регионов по названию")
    public CitySearchResponse search(
            @Parameter(description = "Название города или региона для поиска", example = "Новосибирск")
            @RequestParam("name") String name
    ) {
        return cityService.searchCities(name);
    }
}