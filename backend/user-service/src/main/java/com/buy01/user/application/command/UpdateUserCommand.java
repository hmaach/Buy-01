package com.buy01.user.application.command;

public record UpdateUserCommand(
        String name,
        String email,
        String avatar
        ) {

}
