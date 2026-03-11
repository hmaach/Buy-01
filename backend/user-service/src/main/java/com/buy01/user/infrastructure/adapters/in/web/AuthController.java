package com.buy01.user.infrastructure.adapters.in.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AuthService;
import com.buy01.user.domain.port.in.AvatarService;
import com.buy01.user.domain.port.out.TokenResult;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.LoginRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.RegisterRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.LoginResponse;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/auth")
public class AuthController {

    private final AuthService authService;
    private final AvatarService avatarService;

    @Autowired
    public AuthController(AuthService authService, AvatarService avatarService) {
        this.authService = authService;
        this.avatarService = avatarService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> register(@ModelAttribute @Valid RegisterRequest request) {
        // Save avatar first if provided
        String avatarUrl = avatarService.saveAvatar(request.avatar());

        CreateUserCommand command = new CreateUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.role(),
                avatarUrl
        );

        User user = authService.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginCommand command = new LoginCommand(request.email(), request.password());

        TokenResult tokenResult = authService.login(command);

        return ResponseEntity.ok(
                new LoginResponse(
                        tokenResult.token(),
                        tokenResult.expiresAt().toString()
                )
        );
    }

}
