package org.nsu.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for getting users list with filters and pagination")
public class AdminUserListRequest {

    @Schema(description = "Filters for user list")
    private Filters filters;

    @Schema(description = "Pagination settings")
    private Pagination pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Filters object")
    public static class Filters {
        @Schema(description = "List of statuses to filter by", allowableValues = {"ON_CHECKING", "OK", "BLOCKED"})
        private List<String> statuses;

        @Schema(description = "Email pattern to search for")
        private String emailToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Pagination object")
    public static class Pagination {
        @Schema(description = "Page offset")
        private Integer offset;

        @Schema(description = "Page size")
        private Integer limit;
    }
}
