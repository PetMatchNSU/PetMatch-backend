package org.nsu.authorization.core.exceptions.handlers;

import common.dto.responses.negative.AbstractNegativeResponse;
import common.dto.responses.negative.jwtPerson.PersonErrorResponse;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.exceptions.authorization.PersonHasNotVerifiedEmailException;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthorizationExceptionHandler {

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
    public ResponseEntity<AbstractNegativeResponse> handlePersonHasNotVerifiedEmailException(PersonHasNotVerifiedEmailException e) {
        return  simplePersonResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(JWTIsExpiredException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotAuthorized(JWTIsExpiredException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

}
