package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Moderation lock information")
public class AdminModerationDto {

    @Schema(description = "ID of moderator who locked the user")
    private Long lockedByModeratorId;

    @Schema(description = "Timestamp when user was locked")
    private LocalDateTime lockedAt;
}
