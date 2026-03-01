package com.buy01.user.application.command;

import com.buy01.user.domain.model.Role;

public record RegisterCommand(
        String name,
        String email,
        String password,
        Role role
) {}
