package org.nsu.users.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "Список типов контактов")
public class ContactTypeResponse {

    @Schema(description = "Список типов контактов")
    private List<ContactTypeDto> contactTypes;

    public static final ContactTypeResponse EMPTY = new ContactTypeResponse(List.of());

    public static ContactTypeResponse empty() {
        return EMPTY;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "Тип контакта")
    public static class ContactTypeDto {
        @Schema(description = "Id типа контакта")
        private Long id;
        @Schema(description = "Наименование типа контакта")
        private String type;
    }
}
