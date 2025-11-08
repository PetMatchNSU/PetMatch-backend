package common.dto.responses.negative.jwtPerson;

import lombok.Getter;
import lombok.ToString;
import common.dto.responses.negative.AbstractNegativeResponse;

@ToString
@Getter
public class RegistrationErrorResponse extends AbstractNegativeResponse {
    private final String error;

    public RegistrationErrorResponse(String error, long timestamp) {
        super(timestamp);
        this.error = error;
    }
}
