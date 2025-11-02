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

// Removed unused imports:
// import org.springframework.http.HttpHeaders;
// import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class EmailVerifierController {
    private final EmailVerificationService service;

    /**
     * Handles the email verification request.
     * Spring Security validates the Bearer token and injects the parsed Jwt
     * as the 'principal'.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerifierRequest dto,
            @AuthenticationPrincipal Jwt jwt) {

        // We no longer pass the raw header.
        // We pass the authenticated, validated Jwt object directly to the service.
        service.verifyEmail(dto, jwt);

        // Return a simple 200 OK.
        return ResponseEntity.ok().build();
    }
}