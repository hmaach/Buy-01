package com.buy01.user.application.command;

import java.util.UUID;

public record UpdateUserCommand(
        String name,
        String email,
        UUID avatarId
        ) {

}
