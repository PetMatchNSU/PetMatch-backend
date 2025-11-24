package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to lock user for moderation")
public class AdminUserLockRequest {

    @NotNull
    @Schema(description = "ID of moderator locking the user")
    private Long moderatorId;
}
