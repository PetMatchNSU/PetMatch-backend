package org.nsu.users.core.exceptions.user;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.users.core.dto.AbstractUserNegativeResponse;
import org.nsu.users.core.dto.responses.negative.UserErrorResponseLogin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class UserExceptionHandler {

    private final Tracer tracer;

    private ResponseEntity<AbstractUserNegativeResponse> simplePersonResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity
                .status(httpStatus)
                .body(UserErrorResponseLogin.create(message, tracer));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<AbstractUserNegativeResponse> handlePersonNotFoundException(PersonNotFoundException e) {
        return simplePersonResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
