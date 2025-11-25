package org.nsu.admin.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nsu.admin.dto.AdminUserListRequest;
import org.nsu.admin.dto.AdminUserListResponse;
import org.nsu.admin.dto.LockResponse;
import org.nsu.admin.services.AdminUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "API for competitive moderation of users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    @Operation(summary = "Get users list with filters and pagination",
               description = "Retrieve paginated list of users with status and email filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users list retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public AdminUserListResponse getUsersList(
            @Valid @RequestBody AdminUserListRequest request) {
        AdminUserListResponse response = adminUserService.getUsersList(request);
        return response;
    }

    @PostMapping("/{userId}/lock")
    @Operation(summary = "Lock user for moderation",
               description = "Lock a user to prevent concurrent access during moderation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lock status returned"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public LockResponse lockUser(
            @Parameter(description = "User ID to lock") @PathVariable Long userId) {

        boolean locked = adminUserService.lockUserForModeration(userId);
        return new LockResponse(locked);
    }

    @PostMapping("/{userId}/status")
    @Operation(summary = "Set user status",
               description = "Change user status and optionally add comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value or lock ownership"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public void setUserStatus(
            @Parameter(description = "User ID to update") @PathVariable Long userId,
            @Parameter(description = "New status for the user") @RequestParam String targetStatus,
            @Parameter(description = "Optional reason/comment") @RequestParam(required = false) String reason) {

        adminUserService.setUserStatus(userId, targetStatus, reason);
    }
}
