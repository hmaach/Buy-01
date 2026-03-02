package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email
        @NotNull
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 6)
        String password
        ) {

}
