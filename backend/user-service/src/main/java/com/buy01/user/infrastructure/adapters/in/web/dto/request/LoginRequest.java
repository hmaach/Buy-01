package com.buy01.user.infrastructure.adapters.in.web.dto.request;

public record LoginRequest(
        String email,
        String password
) {}
