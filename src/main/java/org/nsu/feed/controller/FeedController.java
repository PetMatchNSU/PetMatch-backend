package org.nsu.feed.controller;

import org.nsu.feed.dto.requests.animalList.AnimalListRequest;
import org.nsu.feed.dto.responses.animalList.AnimalListResponse;
import org.nsu.feed.service.AnimalListService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/animals")
public class FeedController {

    private AnimalListService animalListService;

    @Operation(summary = "Просмотр ленты с животными", description = "Функционал позволяет пользователям просматривать каталог карточек животных (с возможностью пагинации) и фильтровать их по различным атрибутам (вид, пол, цель размещения и т.д.).")
    @PostMapping(value = "/list")
    public AnimalListResponse listAnimals(@RequestBody AnimalListRequest dto) {
        return animalListService.listAnimals(dto);
    }

}
