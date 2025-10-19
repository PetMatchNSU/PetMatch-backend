package org.nsu.users.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ContactTypeResponse {

    private List<ContactTypeDto> contactTypes;

    @Getter
    @AllArgsConstructor
    public static class ContactTypeDto {
        private Long id;
        private String type;
    }
}
