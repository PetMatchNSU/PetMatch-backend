package org.nsu.authorization.core.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.EmailVerifierRequest;
import org.nsu.authorization.core.services.EmailVerifierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class EmailVerifierController {
    private final EmailVerifierService service;

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerifierRequest dto,
            @RequestHeader("Authorization") String authorizationHeader, @RequestHeader HttpHeaders requestHeaders) {

        service.verifyEmail(dto, authorizationHeader);

        return ResponseEntity
                .ok()
                .headers(requestHeaders)
                .build();

    }
}
