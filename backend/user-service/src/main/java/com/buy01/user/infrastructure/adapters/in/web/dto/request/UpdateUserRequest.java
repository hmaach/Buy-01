package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.application.command.UpdateUserCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
        MultipartFile avatar
        ) {

    public UpdateUserCommand toCommand(UUID avatarId) {
        return new UpdateUserCommand(name, email, avatarId);
    }
}
