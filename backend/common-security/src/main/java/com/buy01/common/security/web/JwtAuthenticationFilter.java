package com.buy01.common.security.web;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.buy01.common.security.jwt.JwtVerifier;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet (Spring MVC) filter. A valid bearer token populates the SecurityContext with an AuthenticatedUser
 * principal; a missing or invalid token leaves the request anonymous so authorization rules decide.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtVerifier verifier;

    public JwtAuthenticationFilter(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BearerToken.from(request.getHeader(HttpHeaders.AUTHORIZATION))
                .flatMap(verifier::verify)
                .ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.authorities())));
        chain.doFilter(request, response);
    }
}
