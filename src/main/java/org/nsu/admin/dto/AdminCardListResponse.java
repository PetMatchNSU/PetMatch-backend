package org.nsu.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminCardListResponse {
    private Long total;
    private List<AdminCardDto> cards;
}
