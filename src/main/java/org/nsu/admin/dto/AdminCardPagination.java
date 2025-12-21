package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Pagination settings for animal card list")
public class AdminCardPagination {
    @Schema(description = "Page offset")
    private Integer offset;

    @Schema(description = "Page size")
    private Integer limit;
}