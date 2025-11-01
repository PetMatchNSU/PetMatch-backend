package org.nsu.authorization.core.dto.responses.negative;

import lombok.Getter;
import lombok.ToString;
import org.nsu.authorization.core.dto.responses.AbstractNegativeResponse;

@ToString
@Getter
public class RegistrationErrorResponse extends AbstractNegativeResponse {
    private final String error;

    public RegistrationErrorResponse(String error, long timestamp) {
        super(timestamp);
        this.error = error;
    }
}
