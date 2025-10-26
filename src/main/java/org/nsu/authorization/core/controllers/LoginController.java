package org.nsu.authorization.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.LoginRequest;
import org.nsu.authorization.core.dto.responses.negative.PersonErrorResponseAuthorization;
import org.nsu.authorization.core.dto.responses.positive.LoginResponse;
import org.nsu.authorization.core.services.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "Authentication", description = "API для авторизации пользователей")
public class LoginController {

    private final LoginService service;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Авторизация пользователя по email и паролю")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная авторизация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Email не подтвержден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponseAuthorization.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Неверный email или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonErrorResponseAuthorization.class)
                    )
            )
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto) {

        return ResponseEntity
                .ok()
                .body(service.login(dto));

    }

}
