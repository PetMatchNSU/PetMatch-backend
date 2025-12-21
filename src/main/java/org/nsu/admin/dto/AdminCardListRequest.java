package org.nsu.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request for getting animal cards list with filters and pagination")
public class AdminCardListRequest {
    @Schema(description = "Filters for animal card list")
    private AdminCardFilters filters;

    @Schema(description = "Pagination settings")
    private AdminCardPagination pagination;
}
