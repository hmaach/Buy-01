package com.buy01.user.infrastructure.adapters.out.security.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.buy01.common.security.jwt.JwtIssuer;
import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.port.out.TokenGeneratorPort;
import com.buy01.user.domain.port.out.TokenResult;

@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final JwtIssuer jwtIssuer;

    public JwtTokenGeneratorAdapter(JwtIssuer jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    @Override
    public TokenResult generateToken(UUID userId, String email, Role role) {
        JwtIssuer.IssuedToken issued = jwtIssuer.issue(userId.toString(), email, role.name());
        return new TokenResult(issued.token(), issued.expiresAt());
    }
}
