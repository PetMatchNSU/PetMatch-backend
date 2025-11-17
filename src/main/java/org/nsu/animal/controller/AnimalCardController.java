package org.nsu.animal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.animal.dto.responses.AnimalCardResponse;
import org.nsu.animal.dto.responses.negative.AnimalErrorResponse;
import org.nsu.animal.service.AnimalCardService;
import common.dto.responses.negative.jwtPerson.PersonErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/animals")
@Tag(name = "Animal Cards", description = "API для управления карточками животных")
public class AnimalCardController {

    private final AnimalCardService animalCardService;

    @PostMapping("/create")
    @Operation(summary = "Создание карточки животного", description = "Создание новой карточки животного")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карточка животного успешно создана"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "При сохранении возникла ошибка, попробуйте позже",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalErrorResponse.class)
                    )
            )
    })
    public void createAnimalCard(@Valid @RequestBody CreateAnimalCardRequest request) {
        animalCardService.createAnimalCard(request);
    }

    @GetMapping("/show/{animalId}")
    @Operation(summary = "Просмотр карточки животного", description = "Получение данных карточки животного по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные карточки животного успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalCardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Карточка животного не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponse.class)
                    )
            )
    })
    public AnimalCardResponse getAnimalCard(@PathVariable Long animalId) {
        return animalCardService.getAnimalCard(animalId);
    }

    @PutMapping("/update")
    @Operation(summary = "Обновление карточки животного", description = "Обновление данных карточки животного")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карточка животного успешно обновлена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса или нет прав для редактирования",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "При сохранении возникла ошибка, попробуйте позже",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalErrorResponse.class)
                    )
            )
    })
    public void updateAnimalCard(@RequestParam Long animalId,
                                 @Valid @RequestBody UpdateAnimalCardRequest request) {
        animalCardService.updateAnimalCard(animalId, request);
    }

    @DeleteMapping("/{animalId}")
    @Operation(summary = "Удаление карточки животного", description = "Удаление карточки животного по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карточка животного успешно удалена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Карточка не найдена или нет прав для удаления",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimalErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponse.class)
                    )
            )
    })
    public void deleteAnimalCard(@PathVariable Long animalId) {
        animalCardService.deleteAnimalCard(animalId);
    }
}
