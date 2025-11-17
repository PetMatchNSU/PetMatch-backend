package org.nsu.animal.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.nsu.animal.dto.responses.negative.AnimalErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "org.nsu.animal")
public class AnimalCardExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AnimalErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Validation error in animal card operation: {}", ex.getMessage());
        AnimalErrorResponse errorResponse = new AnimalErrorResponse(ex.getMessage(), System.currentTimeMillis());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<AnimalErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("State error in animal card operation: {}", ex.getMessage());
        AnimalErrorResponse errorResponse = new AnimalErrorResponse("При сохранении возникла ошибка, попробуйте позже", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AnimalErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        log.error("Validation error: {}", errorMessage);
        AnimalErrorResponse errorResponse = new AnimalErrorResponse(errorMessage, System.currentTimeMillis());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AnimalErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error in animal card operation", ex);
        AnimalErrorResponse errorResponse = new AnimalErrorResponse("При сохранении возникла ошибка, попробуйте позже", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}