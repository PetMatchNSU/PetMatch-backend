package org.nsu.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminCardOwnerDto {
    private Long id;
    private String firstName;
    private String secondName;
    private String lastName;
}
