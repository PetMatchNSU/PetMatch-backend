package org.nsu.authorization.core.controllers;

import common.dto.responses.negative.jwtPerson.PersonErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.RefreshRequest;
import org.nsu.authorization.core.dto.responses.positive.RefreshResponse;
import org.nsu.authorization.core.services.RefreshService;
import org.nsu.users.core.dto.responses.negative.UserErrorResponseLogin;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class RefreshController {

    private final RefreshService refreshService;

    @PostMapping("refresh")
    @Operation(summary = "Обновление токена", description = "Обновление JWT токенов пользователя")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Токены успешно выданы",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RefreshResponse.class)
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
            responseCode = "403",
            description = "Доступ запрещён",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PersonErrorResponse.class)
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
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity
                .ok()
                .body(refreshService.refreshTokens(refreshRequest.getRefreshToken()));
    }
}
