package org.nsu.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockModel {

    private Long id;
    private Long moderatorId;
    private LocalDateTime lockedAt;
    private LocalDateTime expiresAt;
}
