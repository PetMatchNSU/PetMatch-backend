package org.nsu.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.services.UserProfileService;
import org.nsu.users.dto.responses.ContactTypeResponse;
import org.nsu.users.repositories.ContactTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserProfileController {

    private final UserProfileService service;
    private final ContactTypeRepository contactTypeRepository;

    @PutMapping
    public ResponseEntity<?> update(
            @AuthenticationPrincipal PersonDetails principal,
            @Valid @RequestBody UpdateUserRequest dto
    ) {
        service.updateProfile(principal.getUsername(), dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contacts")
    public ResponseEntity<ContactTypeResponse> contactTypes() {
        var list = contactTypeRepository.findAll().stream()
                .map(ct -> new ContactTypeResponse.ContactTypeDto(ct.getId(), ct.getName()))
                .toList();
        return ResponseEntity.ok(new ContactTypeResponse(list));
    }
}