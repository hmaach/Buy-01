package com.buy01.user.infrastructure.adapters.in.web.dto.response;

import java.util.UUID;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        String avatar
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar()
        );
    }
}
