package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information for admin panel")
public class AdminUserDto {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "User status", allowableValues = {"ON_CHECKING", "OK", "BLOCKED"})
    private String status;

    @Schema(description = "User first name")
    private String firstName;

    @Schema(description = "User second name")
    private String secondName;

    @Schema(description = "User last name")
    private String lastName;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "Moderation lock information")
    private AdminModerationDto moderation;
}
