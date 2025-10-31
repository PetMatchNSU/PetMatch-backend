package org.nsu.authorization.core.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.dto.responses.positive.RegistrationResponse;
import org.nsu.authorization.core.services.RegistrationService;
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
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest dto) {

        return service.register(dto);

    }
}
