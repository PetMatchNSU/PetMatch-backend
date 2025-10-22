package org.nsu.authorization.core.exceptions.handlers;

import org.nsu.authorization.core.dto.responses.AbstractNegativeResponse;
import org.nsu.authorization.core.dto.responses.negative.PersonErrorResponse;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.exceptions.authorization.PersonHasNotVerifiedEmailException;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.nsu.authorization.core.exceptions.authorization.RegionNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<AbstractNegativeResponse> simplePersonResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity
                .status(httpStatus)
                .body(new PersonErrorResponse(message, System.currentTimeMillis()));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotFoundException(PersonNotFoundException e) {
        return simplePersonResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PersonHasNotVerifiedEmailException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotFoundException(
            PersonHasNotVerifiedEmailException e) {
        return simplePersonResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(JWTIsExpiredException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotFoundException(JWTIsExpiredException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<AbstractNegativeResponse> HandleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return simplePersonResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegionNotFoundException.class)
    public ResponseEntity<AbstractNegativeResponse> HandleRegionNotFoundException(RegionNotFoundException e) {
        return simplePersonResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<AbstractNegativeResponse> HandleMailException(MailException e) {
        return simplePersonResponse(HttpStatus.CONFLICT, e.getMessage());
    }
}
