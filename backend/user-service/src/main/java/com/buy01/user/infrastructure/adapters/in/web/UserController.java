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

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.UpdateUserRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;
import com.buy01.user.infrastructure.security.JwtAuthenticationFilter.UserPrincipal;
import com.buy01.user.infrastructure.web.client.MediaServiceClient;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final MediaServiceClient mediaServiceClient;

    public UserController(UserService userService, MediaServiceClient mediaServiceClient) {
        this.userService = userService;
        this.mediaServiceClient = mediaServiceClient;
    }

    @GetMapping
    public String test() {
        return "user service is working";
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(currUser.id());
        return ResponseEntity.ok(UserResponse.from(user, null));
    }

    @GetMapping("/id/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id), null);
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @ModelAttribute UpdateUserRequest request
    ) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();

        UUID avatarid = null;
        // Only sellers can upload avatar
        if (currUser.role() == Role.SELLER && request.avatar() != null && !request.avatar().isEmpty()) {
            try {
                avatarid = mediaServiceClient.uploadAvatar(request.avatar());
            } catch (Exception e) {
            }
        }

        User updatedUser = userService.updateUser(currUser.id(), request.toCommand(avatarid));
        return ResponseEntity.ok(UserResponse.from(updatedUser, null));
    }

    @DeleteMapping("/me")
    public void deleteUser(Authentication authentication) {
        UserPrincipal currUser = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(currUser.id());
        userService.deleteUser(user.getId());
    }
}
