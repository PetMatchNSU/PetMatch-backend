package org.nsu.users.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.dto.responses.ContactInfoResponse;
import org.nsu.users.dto.responses.UserProfileResponse;
import org.nsu.users.services.ContactInfoService;
import org.nsu.users.services.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User Profile", description = "API для работы с профилем пользователя")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ContactInfoService contactInfoService;

    @GetMapping
    @Operation(summary = "Просмотр данных пользователя", description = "Получение данных профиля текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные профиля"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Email не подтверждён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public UserProfileResponse getProfile(@AuthenticationPrincipal PersonDetails principal) {
        return userProfileService.getProfile(principal.getUsername());
    }

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
            @Valid @RequestBody UpdateUserRequest dto
    ) {
        userProfileService.updateProfile(principal.getUsername(), dto);
    }

    @GetMapping("/contacts")
    @Operation(summary = "Получение списка контактов", description = "Получение списка контактов, которые будут отображаться другим пользователям в запросе случки/покупки - для редактирования профиля, чтобы получить все возможные списки контактов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список контактов"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    public ContactInfoResponse contactInfo(@AuthenticationPrincipal PersonDetails principal) {
        return contactInfoService.getContactInfo(principal.getUserId());
    }
}