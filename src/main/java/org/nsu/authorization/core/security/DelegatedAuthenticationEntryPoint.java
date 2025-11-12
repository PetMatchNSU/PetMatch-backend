package org.nsu.authorization.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Component
public class DelegatedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    /**
     * Injects the standard Spring MVC HandlerExceptionResolver.
     * This is the bridge that allows Spring Security exceptions to be
     * processed by your global @ControllerAdvice handlers.
     */
    public DelegatedAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {

        // Delegate the exception to Spring MVC's error infrastructure.
        // This prevents the response from being committed prematurely.
        log.warn("Failed to authenticate request entry point for {}", request.getRequestURI(), authException);
        resolver.resolveException(request, response, null, authException);
    }
}