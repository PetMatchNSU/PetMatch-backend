package common.dto.responses.negative.jwtPerson;

import lombok.Getter;
import lombok.ToString;
import common.dto.responses.negative.AbstractNegativeResponse;

@ToString
@Getter
public class RegistrationErrorResponse extends AbstractNegativeResponse {
    private final String message;
    private final String traceId;
    private final String spanId;

    public RegistrationErrorResponse(String message, long timestamp) {
        super(timestamp);
        this.message = message;
        this.traceId = null;
        this.spanId = null;
    }

    public RegistrationErrorResponse(String traceId, String spanId, String message, long timestamp) {
        super(timestamp);
        this.message = message;
        this.traceId = traceId;
        this.spanId = spanId;
    }
}
