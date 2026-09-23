package com.recruitcrm.rts.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitcrm.rts.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Security rules for the REST API:
 *  - HTTP Basic authentication (email + password sent with every request; in Postman use the "Basic Auth" tab)
 *  - stateless: no session, no cookies
 *  - CSRF is disabled because there is no browser session to protect
 *  - role rules for each endpoint are written next to the endpoint with @PreAuthorize
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

        // 401: no / wrong email+password
        AuthenticationEntryPoint unauthorized = (request, response, ex) -> {
            response.setHeader("WWW-Authenticate", "Basic realm=\"RTS\"");
            writeError(response, objectMapper, HttpStatus.UNAUTHORIZED,
                    "Login required: send a valid email and password (Basic Auth).");
        };

        // 403: logged in, but not allowed
        AccessDeniedHandler forbidden = (request, response, ex) ->
                writeError(response, objectMapper, HttpStatus.FORBIDDEN,
                        "You do not have permission to do this.");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public: register, and reading jobs
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/my").authenticated()   // must stay above the next rule
                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/*").permitAll()
                        // everything else needs a login (exact role is checked with @PreAuthorize)
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorized))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden));

        return http.build();
    }

    /** Errors raised by the security filters are outside the controllers, so we write the JSON here. */
    private static void writeError(HttpServletResponse response, ObjectMapper objectMapper,
                                   HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiError body = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, null);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
