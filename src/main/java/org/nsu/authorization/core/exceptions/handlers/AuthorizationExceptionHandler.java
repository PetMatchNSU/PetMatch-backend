package org.nsu.authorization.core.exceptions.handlers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import common.dto.responses.negative.AbstractNegativeResponse;
import common.dto.responses.negative.jwtPerson.PersonErrorResponse;
import org.nsu.authorization.core.exceptions.authorization.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

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
        return simplePersonResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(JWTIsExpiredException.class)
    public ResponseEntity<AbstractNegativeResponse> handlePersonNotAuthorized(JWTIsExpiredException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(EmailVerificationFailException.class)
    public ResponseEntity<AbstractNegativeResponse> handleEmailVerificationFailException(EmailVerificationFailException e) {
        return simplePersonResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(VerificationCodeGenerationFailException.class)
    public ResponseEntity<AbstractNegativeResponse> handleVerificationCodeGenerationFailException(VerificationCodeGenerationFailException e) {
        return simplePersonResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * Handles exceptions from the JWTFilter (e.g., bad signature, expired token, malformed).
     * This catches the *actual* exception your filter is delegating.
     */
    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<AbstractNegativeResponse> handleJWTVerificationException(JWTVerificationException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, "Invalid Token: " + e.getMessage());
    }

    /**
     * Handles exceptions from DelegatedAuthenticationEntryPoint.
     * This is for when authentication is required but missing (e.g., no Bearer token).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AbstractNegativeResponse> handleAuthenticationException(AuthenticationException e) {
        return simplePersonResponse(HttpStatus.UNAUTHORIZED, "Authentication Required");
    }

    /**
     * Handles exceptions from DelegatedAccessDeniedHandler.
     * This is for when the user is authenticated but not authorized (e.g., wrong role).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AbstractNegativeResponse> handleAccessDeniedException(AccessDeniedException e) {
        return simplePersonResponse(HttpStatus.FORBIDDEN, "Access Denied");
    }
}
