package org.nsu.authorization.core.exceptions.handlers;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.common.dto.responses.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        
        String message;
        HttpStatus status;
        
        if (ex.getMessage() != null && (ex.getMessage().contains("email") || ex.getMessage().contains("unique"))) {
            message = "A user with this email already exists.";
            status = HttpStatus.CONFLICT;
        } else {
            message = "Data integrity violation";
            status = HttpStatus.BAD_REQUEST;
        }
        
        ApiErrorResponse error = ApiErrorResponse.create(message, tracer);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for request", ex);
        
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        
        ApiErrorResponse error = ApiErrorResponse.create(message, tracer);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.create(ex.getMessage(), tracer);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime exception: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.create(ex.getMessage(), tracer);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({NullPointerException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleUnexpectedExceptions(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ApiErrorResponse error = ApiErrorResponse.create("Internal server error", tracer);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
