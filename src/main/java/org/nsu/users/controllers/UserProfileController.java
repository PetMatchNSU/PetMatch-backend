package org.nsu.users.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.dto.responses.ContactTypeResponse;
import org.nsu.users.services.ContactTypeService;
import org.nsu.users.services.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.nsu.users.dto.responses.UserAnimalListResponse;
import org.nsu.users.services.UserAnimalListService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User Profile", description = "API для работы с профилем пользователя")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ContactTypeService contactTypeService;
    private final UserAnimalListService userAnimalListService;

    @PutMapping
    @Operation(summary = "Обновление профиля пользователя", description = "Обновление данных пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "404", description = "Пользователь или регион не найден")
    })
    public void update(
            @AuthenticationPrincipal PersonDetails principal,
            @Valid @RequestBody UpdateUserRequest dto) {
        userProfileService.updateProfile(principal.getUsername(), dto);
    }

    @GetMapping("/contacts")
    @Operation(summary = "Получение списка типов контактов", description = "Получение списка доступных типов контактов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список типов контактов"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    public ContactTypeResponse contactTypes() {
        return contactTypeService.getAllContactTypes();
    }

    @GetMapping("/animals/list")
    public UserAnimalListResponse animalList(@AuthenticationPrincipal PersonDetails principal) {
        return userAnimalListService.getUserAnimalList(principal.getUserId());
    }

}