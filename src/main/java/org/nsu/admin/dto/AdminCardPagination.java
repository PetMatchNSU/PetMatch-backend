package org.nsu.admin.dto;

import lombok.Data;

@Data
public class AdminCardPagination {
    private Integer offset;
    private Integer limit;
}
