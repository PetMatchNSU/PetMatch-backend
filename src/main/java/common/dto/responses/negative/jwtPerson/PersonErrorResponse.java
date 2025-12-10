package common.dto.responses.negative.jwtPerson;

import lombok.Getter;
import lombok.ToString;
import common.dto.responses.negative.AbstractNegativeResponse;
import org.nsu.common.utils.TracingUtils;
import io.micrometer.tracing.Tracer;

@ToString
@Getter
public class PersonErrorResponse extends AbstractNegativeResponse {

    private final String message;
    private final String traceId;
    private final String spanId;

    public PersonErrorResponse(String message, long timestamp) {
        super(timestamp);
        this.message = message;
        this.traceId = null;
        this.spanId = null;
    }

    public PersonErrorResponse(String traceId, String spanId, String message, long timestamp) {
        super(timestamp);
        this.message = message;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public static PersonErrorResponse create(String message, Tracer tracer) {
        String traceId = TracingUtils.getCurrentTraceId(tracer);
        String spanId = TracingUtils.getCurrentSpanId(tracer);
        
        return new PersonErrorResponse(traceId, spanId, message, System.currentTimeMillis());
    }
}
