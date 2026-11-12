package com.buy01.user.domain.port.out;

import java.util.UUID;

import com.buy01.user.domain.model.Role;

public interface TokenGeneratorPort {

    TokenResult generateToken(UUID userId, String email, Role role);
}
