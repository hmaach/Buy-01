package com.buy01.user.infrastructure.adapters.in.web.dto.request;

public record CreateUserRequest(
        String name,
        String email,
        String password
        ) {

}
