package org.nsu.authorization.core.exceptions.handlers;

import org.nsu.authorization.core.dto.responses.AbstractAuthorizationNegativeResponse;
import org.nsu.authorization.core.dto.responses.negative.PersonErrorResponseAuthorization;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.exceptions.authorization.PersonHasNotVerifiedEmailException;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthorizationExceptionHandler {

    private ResponseEntity<AbstractAuthorizationNegativeResponse> simplePersonResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity
                .status(httpStatus)
                .body(new PersonErrorResponseAuthorization(message, System.currentTimeMillis()));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<AbstractAuthorizationNegativeResponse> handlePersonNotFoundException(PersonNotFoundException e) {
        return simplePersonResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PersonHasNotVerifiedEmailException.class)
    public ResponseEntity<AbstractAuthorizationNegativeResponse> handlePersonHasNotVerifiedEmailException(PersonHasNotVerifiedEmailException e) {
        return  simplePersonResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(JWTIsExpiredException.class)
    public ResponseEntity<AbstractAuthorizationNegativeResponse> handlePersonNotAuthorized(JWTIsExpiredException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

}
