package org.nsu.common.dto.responses;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        ApiErrorResponse error = new ApiErrorResponse(message);
        
        if (tracer != null) {
            Span currentSpan = tracer.nextSpan().start();
            if (currentSpan != null) {
                error.setTraceId(currentSpan.context().traceId());
                error.setSpanId(currentSpan.context().spanId());
                currentSpan.end();
            }
        }
        
        return error;
    }
}