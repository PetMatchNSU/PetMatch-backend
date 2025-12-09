package org.nsu.common.dto.responses;

import io.micrometer.tracing.Tracer;
import lombok.Getter;
import lombok.Setter;
import org.nsu.common.utils.TracingUtils;

@Getter
@Setter
public class ApiErrorResponse {
    private String traceId;
    private String spanId;
    private String message;
    private long timestamp;

    public ApiErrorResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiErrorResponse(String traceId, String spanId, String message, long timestamp) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static ApiErrorResponse create(String message, Tracer tracer) {
        String traceId = TracingUtils.getCurrentTraceId(tracer);
        String spanId = TracingUtils.getCurrentSpanId(tracer);
        
        return new ApiErrorResponse(traceId, spanId, message, System.currentTimeMillis());
    }
}