package org.nsu.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Filters for animal card list")
public class AdminCardFilters {
    @Schema(description = "List of statuses to filter by")
    private List<String> statuses;

    @Schema(description = "List of goals to filter by")
    private List<String> goals;

    @Schema(description = "Filter by creation date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @Schema(description = "Filter by last update date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
}
