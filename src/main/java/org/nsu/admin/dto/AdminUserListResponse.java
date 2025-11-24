package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing users list with pagination info")
public class AdminUserListResponse {

    @Schema(description = "Total number of records")
    private Long total;

    @Schema(description = "List of users")
    private List<AdminUserDto> users;
}
