package org.nsu.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AdminCardDto {
    private Long cardId;
    private Long animalId;
    private String status;
    private String goal;
    private String cardName;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private AdminCardOwnerDto owner;
    private AdminModerationDto moderation;
}
