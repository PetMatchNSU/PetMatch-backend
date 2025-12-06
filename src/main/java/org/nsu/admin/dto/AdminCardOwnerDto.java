package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Animal card owner information")
public class AdminCardOwnerDto {
    @Schema(description = "Owner user ID")
    private Long id;

    @Schema(description = "Owner first name")
    private String firstName;

    @Schema(description = "Owner second name")
    private String secondName;

    @Schema(description = "Owner last name")
    private String lastName;
}
