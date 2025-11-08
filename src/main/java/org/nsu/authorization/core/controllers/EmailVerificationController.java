package org.nsu.authorization.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.EmailVerificationRequest;
import org.nsu.authorization.core.services.EmailVerificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Management", description = "Operations related to user accounts (e.g., email verification)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@SecurityRequirement(name = "bearerAuth")
public class EmailVerificationController {
    private final EmailVerificationService service;

    /**
     * Handles the email verification request.
     * Spring Security validates the Bearer token and injects the parsed Jwt
     * as the 'principal'.
     */
    @PostMapping("/verify-email")
    @Operation(
            summary = "Подтверждение email", // Summary for the operation
            description = "Подтверждает email пользователя с использованием кода, присланного после регистрации. Требует Bearer-токен.",
            requestBody = @RequestBody(
                    description = "Код подтверждения из письма",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmailVerificationRequest.class),
                            examples = @ExampleObject(
                                    name = "Verification Request",
                                    value = "{\"code\": \"eqtr6q798wet6\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Email успешно подтвержден. Пользователь может быть переведен на страницу 'Лента'.",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Success Response", value = "{}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный код или срок действия истёк. Мы выслали новое письмо для подтверждения почты.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Error Response",
                                            value = "{\"message\": \"Неверный код или срок действия истёк. Мы выслали новое письмо для подтверждения почты\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Не предоставлен или недействителен Bearer-токен."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "У сервера произошла внутренняя ошибка, которая не позволила корректно обработать запрос клиента"
                    )
            }
    )
    public void verifyEmail(@Valid @RequestBody EmailVerificationRequest dto, @AuthenticationPrincipal Jwt jwt) {

        service.verifyEmail(dto, jwt);

    }
}