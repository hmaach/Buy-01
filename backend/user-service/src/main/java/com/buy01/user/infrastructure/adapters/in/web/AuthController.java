package com.buy01.user.infrastructure.adapters.in.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.application.command.RegisterCommand;
import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AuthService;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.LoginRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.RegisterRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.LoginResponse;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;
import com.buy01.user.infrastructure.web.client.MediaServiceClient;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final MediaServiceClient mediaServiceClient;

    @Autowired
    public AuthController(AuthService authService, MediaServiceClient mediaServiceClient) {
        this.authService = authService;
        this.mediaServiceClient = mediaServiceClient;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        RegisterCommand command = new RegisterCommand(
                request.name(),
                request.email(),
                request.password(),
                request.role() != null ? request.role() : Role.CLIENT
        );
        
        User user = authService.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.email(), request.password());
        String token = authService.login(command);
        
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = authService.getCurrentUser(email);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MultipartFile avatar
    ) {
        String email = authentication.getName();
        User currentUser = authService.getCurrentUser(email);
        
        String avatarUrl = null;
        // Only sellers can upload avatar
        if (currentUser.getRole() == Role.SELLER && avatar != null && !avatar.isEmpty()) {
            try {
                avatarUrl = mediaServiceClient.uploadAvatar(avatar);
            } catch (Exception e) {
                // Log error but continue with update
            }
        }
        
        User updatedUser = authService.updateCurrentUser(email, name, avatarUrl);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }
}
