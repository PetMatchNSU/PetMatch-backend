package org.nsu.users.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User Profile", description = "API для работы с профилем пользователя")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ContactTypeService contactTypeService;

    @PutMapping
    @Operation(summary = "Обновление профиля пользователя", description = "Обновление данных пользователя")
    @SecurityRequirement(name = "Bearer Authentication")
    public void update(
            @AuthenticationPrincipal PersonDetails principal,
            @Valid @RequestBody UpdateUserRequest dto
    ) {
        userProfileService.updateProfile(principal.getUsername(), dto);
    }

    @GetMapping("/contacts")
    @Operation(summary = "Получение списка типов контактов", description = "Получение списка доступных типов контактов")
    @SecurityRequirement(name = "Bearer Authentication")
    public ContactTypeResponse contactTypes() {
        return contactTypeService.getAllContactTypes();
    }
}