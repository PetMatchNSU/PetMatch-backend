package org.nsu.users.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAnimalListRequest {

    @Schema(description = "идентификатор пользователя, показывает, что в ответе надо отдавать только питомцев этого пользователя", example = "1233445")
    private Long userId;
}
