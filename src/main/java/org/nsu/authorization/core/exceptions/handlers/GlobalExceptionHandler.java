package org.nsu.authorization.core.exceptions.handlers;

import org.nsu.authorization.core.dto.responses.AbstractNegativeResponse;
import org.nsu.authorization.core.dto.responses.negative.PersonErrorResponse;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.exceptions.authorization.PersonHasNotVerifiedEmailException;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<AbstractNegativeResponse> simplePersonResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity
                .status(httpStatus)
                .body(new PersonErrorResponse(Map.of("error", message), System.currentTimeMillis()));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotFoundException(PersonNotFoundException e) {
        return simplePersonResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PersonHasNotVerifiedEmailException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotFoundException(PersonHasNotVerifiedEmailException e) {
        return  simplePersonResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(JWTIsExpiredException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotFoundException(JWTIsExpiredException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }



}
