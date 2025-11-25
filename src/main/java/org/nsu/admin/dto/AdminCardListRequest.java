package org.nsu.admin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AdminCardListRequest {
    private AdminCardFilters filters;
    private AdminCardPagination pagination;
}
