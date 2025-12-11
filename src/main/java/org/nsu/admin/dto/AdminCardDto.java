package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Animal card information for admin panel")
public class AdminCardDto {
    @Schema(description = "Card ID")
    private Long cardId;

    @Schema(description = "Animal ID")
    private Long animalId;

    @Schema(description = "Card status")
    private String status;

    @Schema(description = "Placement goal")
    private String goal;

    @Schema(description = "Card name")
    private String cardName;

    @Schema(description = "Card creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Card last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Card owner information")
    private AdminCardOwnerDto owner;

    @Schema(description = "Moderation lock information")
    private AdminModerationDto moderation;
}
