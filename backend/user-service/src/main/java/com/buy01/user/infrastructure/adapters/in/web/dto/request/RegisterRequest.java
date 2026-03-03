package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import com.buy01.user.domain.model.Role;

public record RegisterRequest(
        String name,
        String email,
        String password,
        Role role
) {}
