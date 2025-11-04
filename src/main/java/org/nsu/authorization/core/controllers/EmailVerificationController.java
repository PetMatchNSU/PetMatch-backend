package org.nsu.authorization.core.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.EmailVerifierRequest;
import org.nsu.authorization.core.services.EmailVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class EmailVerificationController {
    private final EmailVerificationService service;

    /**
     * Handles the email verification request.
     * Spring Security validates the Bearer token and injects the parsed Jwt
     * as the 'principal'.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerifierRequest dto,
                                         @AuthenticationPrincipal Jwt jwt) {

        service.verifyEmail(dto, jwt);

        return ResponseEntity.ok().build();
    }
}