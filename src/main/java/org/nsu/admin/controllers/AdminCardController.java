package org.nsu.admin.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.admin.dto.AdminCardListRequest;
import org.nsu.admin.dto.AdminCardListResponse;
import org.nsu.admin.dto.LockResponse;
import org.nsu.admin.services.AdminCardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin Card Management", description = "API for competitive moderation of animal cards")
public class AdminCardController {

    private final AdminCardService adminCardService;

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR')")
    @Operation(summary = "Get card list with filters and pagination",
               description = "Retrieve paginated list of cards with status, goal, date filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cards list retrieved successfully")
    })
    public AdminCardListResponse getCardsList(@Valid AdminCardListRequest request) {
        return adminCardService.getCardsList(request);
    }

    @PostMapping("/{card_id}/lock")
    @PreAuthorize("hasRole('MODERATOR')")
    @Operation(summary = "Lock card for moderation",
               description = "Lock a card to prevent concurrent access during moderation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lock status returned")
    })
    public LockResponse lockCard(@Parameter(description = "Card ID to lock") @PathVariable("card_id") Long cardId) {
        boolean locked = adminCardService.lockCardForModeration(cardId);
        return new LockResponse(locked);
    }

    @PostMapping("/{card_id}/status")
    @PreAuthorize("hasRole('MODERATOR')")
    @Operation(summary = "Set card status after moderation",
               description = "Change card status and optionally add comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card status updated successfully")
    })
    public void setCardStatus(
            @Parameter(description = "Card ID to update") @PathVariable("card_id") Long cardId,
            @Parameter(description = "New status for the card") @RequestParam String targetStatus,
            @Parameter(description = "Optional reason/comment") @RequestParam(required = false) String reason) {
        adminCardService.setCardStatus(cardId, targetStatus, reason);
    }
}
