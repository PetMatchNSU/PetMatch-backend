package org.nsu.common.utils;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@UtilityClass
public class TracingUtils {

    public static String getCurrentTraceId(Tracer tracer) {
        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null && currentSpan.context() != null) {
                return currentSpan.context().traceId();
            }
        }
        
        // Fallback: try to get from HTTP headers if available
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String traceId = request.getHeader("X-Trace-Id");
                if (traceId == null) {
                    traceId = request.getHeader("traceparent");
                    if (traceId != null && traceId.length() > 3) {
                        // Extract trace ID from traceparent header (W3C format)
                        String[] parts = traceId.split("-");
                        if (parts.length >= 2) {
                            return parts[1];
                        }
                    }
                }
                return traceId;
            }
        } catch (Exception e) {
            // Ignore - no request context available
        }
        
        return null;
    }

    public static String getCurrentSpanId(Tracer tracer) {
        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null && currentSpan.context() != null) {
                return currentSpan.context().spanId();
            }
        }
        
        // Fallback: try to get from HTTP headers if available
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String spanId = request.getHeader("X-Span-Id");
                if (spanId == null) {
                    String traceParent = request.getHeader("traceparent");
                    if (traceParent != null) {
                        // Extract span ID from traceparent header (W3C format)
                        String[] parts = traceParent.split("-");
                        if (parts.length >= 3) {
                            return parts[2];
                        }
                    }
                }
                return spanId;
            }
        } catch (Exception e) {
            // Ignore - no request context available
        }
        
        return null;
    }
}