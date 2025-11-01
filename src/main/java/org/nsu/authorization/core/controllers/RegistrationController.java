package org.nsu.authorization.core.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.dto.responses.negative.RegistrationErrorResponse;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.services.RegistrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "Пользователи", description = "Методы для регистрации и авторизации пользователей")
public class RegistrationController {
    private final RegistrationService service;

    @Operation(summary = "Регистрация пользователя через Email", description = "Создает нового пользователя, отправляет код подтверждения на почту и возвращает JWT токены.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация. Пользователь создан, письмо отправлено.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RegistrationResponse.class), examples = @ExampleObject(name = "Успешный ответ", value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"user\": {\"isEmailVerified\": false}}"))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RegistrationErrorResponse.class), examples = @ExampleObject(name = "Пример ошибки валидации", value = "{\"statusCode\": 400, \"message\": \"Email should be valid\", \"timestamp\": 1678886400000}"))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RegistrationErrorResponse.class), examples = @ExampleObject(name = "Пример ошибки 409", value = "{\"statusCode\": 409, \"message\": \"Пользователь с таким email уже существует\", \"timestamp\": 1678886400000}"))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (сбой базы данных, ошибки отправки почты).", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RegistrationErrorResponse.class), examples = @ExampleObject(name = "Пример ошибки 500", value = "{\"statusCode\": 500, \"message\": \"Сервис временно недоступен, приносим извинения за неудобства\", \"timestamp\": 1678886400000}")))
    })
    @PostMapping("/register")
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest dto) {

        return service.register(dto);

    }
}
