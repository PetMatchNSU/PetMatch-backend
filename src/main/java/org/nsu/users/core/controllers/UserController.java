package org.nsu.users.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.responses.negative.PersonErrorResponseAuthorization;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.core.dto.responses.negative.UserErrorResponseLogin;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Просмотр профиля пользователя", description = "Получение данных текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные пользователя успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponseAuthorization.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponseAuthorization.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserErrorResponseLogin.class)
                    )
            )
    })
    public ResponseEntity<UserResponse> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        
        Long userId = personDetails.getUserId();
        UserResponse userResponse = userService.getUserProfile(userId);
        
        return ResponseEntity.ok(userResponse);
    }
}
