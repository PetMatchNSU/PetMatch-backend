package org.nsu.authorization.core.exceptions.handlers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.common.dto.responses.ApiErrorResponse;
import org.nsu.authorization.core.exceptions.authorization.JWTIsExpiredException;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.nsu.authorization.core.exceptions.authorization.UserAlreadyExistsException;
import org.nsu.authorization.core.exceptions.authorization.RegionNotFoundException;
import org.nsu.authorization.core.exceptions.authorization.UserCreationFailException;
import org.nsu.authorization.core.exceptions.authorization.EmailVerificationFailException;
import org.nsu.authorization.core.exceptions.authorization.VerificationCodeGenerationFailException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class AuthorizationExceptionHandler {

    private final Tracer tracer;

    private ResponseEntity<ApiErrorResponse> createErrorResponse(HttpStatus httpStatus, String message) {
        ApiErrorResponse errorResponse = ApiErrorResponse.create(message, tracer);
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePersonNotFoundException(PersonNotFoundException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(JWTIsExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handlePersonNotAuthorized(JWTIsExpiredException e) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRegionNotFoundException(RegionNotFoundException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Region not found: " + e.getMessage());
    }
    
    @ExceptionHandler(org.nsu.users.exceptions.RegionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsersRegionNotFoundException(org.nsu.users.exceptions.RegionNotFoundException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Region not found: " + e.getMessage());
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiErrorResponse> handleMailException(MailException e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send mail: " + e.getMessage());
    }

    @ExceptionHandler(UserCreationFailException.class)
    public ResponseEntity<ApiErrorResponse> handleUserCreationFailException(UserCreationFailException e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user: " + e.getMessage());
    }

    @ExceptionHandler(EmailVerificationFailException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailVerificationFailException(EmailVerificationFailException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<ApiErrorResponse> handleJWTVerificationException(JWTVerificationException e) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Token: " + e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access Denied");
    }

    @ExceptionHandler(VerificationCodeGenerationFailException.class)
    public ResponseEntity<ApiErrorResponse> handleVerificationCodeGenerationFailException(VerificationCodeGenerationFailException e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}