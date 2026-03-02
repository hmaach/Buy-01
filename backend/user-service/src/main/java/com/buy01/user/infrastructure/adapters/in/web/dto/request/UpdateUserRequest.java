package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import com.buy01.user.application.command.UpdateUserCommand;

public record UpdateUserRequest(
        String name,
        String email,
        String avatar
        ) {

    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(name, email, avatar);
    }
}
