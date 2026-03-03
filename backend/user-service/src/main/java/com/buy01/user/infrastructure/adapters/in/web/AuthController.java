package com.buy01.user.infrastructure.adapters.in.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AuthService;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.LoginRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.RegisterRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.LoginResponse;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.role() != null ? request.role() : Role.CLIENT,
                null
        );

        User user = authService.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginCommand command = new LoginCommand(request.email(), request.password());
        String token = authService.login(command);

        return ResponseEntity.ok(new LoginResponse(token));
    }

}
