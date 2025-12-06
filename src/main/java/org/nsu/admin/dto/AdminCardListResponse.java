package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Response containing animal cards list with pagination info")
public class AdminCardListResponse {
    @Schema(description = "Total number of records")
    private Long total;

    @Schema(description = "List of animal cards")
    private List<AdminCardDto> cards;
}
