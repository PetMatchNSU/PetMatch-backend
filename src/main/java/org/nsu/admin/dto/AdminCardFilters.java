package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Filters for animal card list")
public class AdminCardFilters {
    @Schema(description = "List of statuses to filter by")
    private List<String> statuses;

    @Schema(description = "List of goals to filter by")
    private List<String> goals;

    @Schema(description = "Filter by creation date and time")
    private LocalDateTime createdAt;

    @Schema(description = "Filter by last update date and time")
    private LocalDateTime updatedAt;
}
