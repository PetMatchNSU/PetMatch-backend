package org.nsu.users.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "Список контактов")
public class ContactInfoResponse {

    @Schema(description = "Список контактов")
    private List<ContactInfoDto> contactInfo;

    public static final ContactInfoResponse EMPTY = new ContactInfoResponse(List.of());

    public static ContactInfoResponse empty() {
        return EMPTY;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "Информация о контакте")
    public static class ContactInfoDto {
        @Schema(description = "Тип контакта")
        private String type;

        @Schema(description = "Способ связи для данного контакта")
        private String contact;
    }
}
