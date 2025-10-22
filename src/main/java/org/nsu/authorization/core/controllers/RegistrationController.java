package org.nsu.authorization.core.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.dto.requests.RegistrationRequest;
import org.nsu.authorization.core.services.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class RegistrationController {
    private final RegistrationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest dto) {

        return ResponseEntity
                .ok()
                .body(service.register(dto));

    }
}
