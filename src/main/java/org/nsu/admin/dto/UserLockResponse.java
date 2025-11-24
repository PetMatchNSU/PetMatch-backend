package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for user lock operation")
public class UserLockResponse {

    @Schema(description = "Whether the lock was successfully acquired")
    private boolean isLockAcquired;
}
