package com.buy01.user.infrastructure.security;

import java.util.Collections;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.port.out.TokenGeneratorPort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenGeneratorPort tokenGenerator;

    public JwtAuthenticationFilter(TokenGeneratorPort tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, java.io.IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                if (tokenGenerator.validateToken(token)) {
                    String email = tokenGenerator.extractEmail(token);
                    UUID userId = tokenGenerator.extractUserId(token);
                    Role role = tokenGenerator.extractRole(token);
                    
                    UserPrincipal principal = new UserPrincipal(userId, email, role);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token validation failed, continue without authentication
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    public record UserPrincipal(UUID id, String email, Role role) {}
}
