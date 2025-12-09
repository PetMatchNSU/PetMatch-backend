package org.nsu.users.core.dto.responses.negative;

import lombok.Getter;
import lombok.ToString;
import org.nsu.users.core.dto.AbstractUserNegativeResponse;
import org.nsu.common.utils.TracingUtils;
import io.micrometer.tracing.Tracer;

@ToString
@Getter
public class UserErrorResponseLogin extends AbstractUserNegativeResponse {

    private final String message;
    private final String traceId;
    private final String spanId;

    public UserErrorResponseLogin(String message, long timestamp) {
        super(timestamp);
        this.message = message;
        this.traceId = null;
        this.spanId = null;
    }

    public UserErrorResponseLogin(String traceId, String spanId, String message, long timestamp) {
        super(timestamp);
        this.message = message;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public static UserErrorResponseLogin create(String message, Tracer tracer) {
        String traceId = TracingUtils.getCurrentTraceId(tracer);
        String spanId = TracingUtils.getCurrentSpanId(tracer);
        
        return new UserErrorResponseLogin(traceId, spanId, message, System.currentTimeMillis());
    }
}
