package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import com.buy01.user.domain.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        String name,
        @Email
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 6)
        String password,
        @Pattern(regexp = "SELLER|CLIENT", message = "Role must be 'SELLER' or 'CLIENT'")
        Role role
        ) {

}
