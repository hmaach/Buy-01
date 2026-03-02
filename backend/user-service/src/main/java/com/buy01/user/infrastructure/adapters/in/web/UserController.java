package com.buy01.user.infrastructure.adapters.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AuthService;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;
import com.buy01.user.infrastructure.web.client.MediaServiceClient;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final MediaServiceClient mediaServiceClient;

    public UserController(UserService userService, AuthService authService, MediaServiceClient mediaServiceClient) {
        this.userService = userService;
        this.authService = authService;
        this.mediaServiceClient = mediaServiceClient;
    }

    @GetMapping
    public String test() {
        return "user service is working";
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getPrincipal().toString();
        System.out.println("Authenticated user email: " + email);
        User user = authService.getCurrentUser(email);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/id/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id));
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
            }
        }

        User updatedUser = authService.updateCurrentUser(email, name, avatarUrl);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @DeleteMapping("/me")
    public void deleteUser(Authentication authentication) {
        String email = authentication.getName();
        User user = authService.getCurrentUser(email);
        userService.deleteUser(user.getId());
    }
}
