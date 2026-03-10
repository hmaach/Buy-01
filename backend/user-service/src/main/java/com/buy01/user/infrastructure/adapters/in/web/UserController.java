package com.buy01.user.infrastructure.adapters.in.web;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AvatarService;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.UpdateUserRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;
import com.buy01.user.infrastructure.security.JwtAuthenticationFilter.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AvatarService avatarService;

    public UserController(UserService userService, AvatarService avatarService) {
        this.userService = userService;
        this.avatarService = avatarService;
    }

    @GetMapping
    public String test() {
        return "user service is working";
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(currUser.id());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/id/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id));
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @ModelAttribute UpdateUserRequest request
    ) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();

        String avatarUrl = null;
        if (request.avatar() != null && !request.avatar().isEmpty()) {
            avatarUrl = avatarService.saveAvatar(request.avatar());
        }

        User updatedUser = userService.updateUser(currUser.id(), request.toCommand(avatarUrl));
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @DeleteMapping("/me")
    public void deleteUser(Authentication authentication) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(currUser.id());
        userService.deleteUser(user.getId());
    }
}
