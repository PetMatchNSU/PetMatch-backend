package org.nsu.authorization.core.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nsu.authorization.core.services.PersonDetailsService;
import org.nsu.authorization.core.utils.JWTTypes;
import org.nsu.authorization.core.services.JWTService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTService jwtService;
    private final PersonDetailsService personDetailsService;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver resolver;

    public JWTFilter(JWTService jwtService,
                     PersonDetailsService personDetailsService,
                     @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.personDetailsService = personDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String JWTToken = header.replaceFirst(BEARER_PREFIX, "");

        try {
            if (JWTToken.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JWT Token (Blank)");
            }

            long id = Long.parseLong(jwtService.extractClaim(JWTToken, JWTTypes.ACCESS_TOKEN, "userID"));
            UserDetails userDetails = personDetailsService.loadUserById(id);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JWTVerificationException e) {

            SecurityContextHolder.getContext().setAuthentication(null);
            resolver.resolveException(request, response, null, e);

        } catch (ResponseStatusException e) {

            resolver.resolveException(request, response, null, e);

        } catch (Exception e) {

            SecurityContextHolder.getContext().setAuthentication(null);
            resolver.resolveException(request, response, null, e);
        }
    }
}
