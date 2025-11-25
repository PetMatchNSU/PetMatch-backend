package org.nsu.admin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AdminCardFilters {
    private List<String> statuses;
    private List<String> goals;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
