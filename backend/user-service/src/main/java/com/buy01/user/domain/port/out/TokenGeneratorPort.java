package com.buy01.user.domain.port.out;

import java.util.UUID;

import com.buy01.user.domain.model.Role;

public interface TokenGeneratorPort {
    
    String generateToken(UUID userId, String email, Role role);
    
    UUID extractUserId(String token);
    
    String extractEmail(String token);
    
    Role extractRole(String token);
    
    boolean validateToken(String token);
}
