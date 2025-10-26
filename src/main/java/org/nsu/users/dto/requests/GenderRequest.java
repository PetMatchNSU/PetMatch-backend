package org.nsu.users.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Пол пользователя: M - Мужской (Male), F - Женский (Female)")
public enum GenderRequest {
    M,
    F
}