package org.nsu.feed.controller;

import lombok.RequiredArgsConstructor;

import org.nsu.feed.dto.requests.AnimalListRequest;
import org.nsu.feed.dto.responses.AnimalInfoResponse;
import org.nsu.feed.dto.responses.animalList.AnimalListResponse;
import org.nsu.feed.service.AnimalListService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.nsu.feed.service.AnimalInfoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class FeedController {

    private final AnimalListService animalListService;
    private final AnimalInfoService animalInfoService;

    @Operation(summary = "Просмотр ленты с животными", description = "Функционал позволяет пользователям просматривать каталог карточек животных (с возможностью пагинации) и фильтровать их по различным атрибутам (вид, пол, цель размещения и т.д.).")
    @PostMapping(value = "/list")
    public AnimalListResponse listAnimals(@RequestBody AnimalListRequest dto) {
        return animalListService.listAnimals(dto);
    }

    @Operation(summary = "Просмотр информации о животных", description = "Запрос для получения списка видов животных, целью размещения карточки")
    @GetMapping(value = "/info")
    public AnimalInfoResponse getAnimalInfo() {
        return animalInfoService.getAnimalInfo();
    }

}
