package com.buy01.user.application.command;

public record LoginCommand(
        String email,
        String password
) {}
