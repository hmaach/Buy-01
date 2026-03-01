package com.buy01.user.infrastructure.adapters.in.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.CreateUserRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.request.UpdateUserRequest;
import com.buy01.user.infrastructure.adapters.in.web.dto.response.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String test() {
        return "user service is working";
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        throw new UnsupportedOperationException();
    }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {

        CreateUserCommand command
                = new CreateUserCommand(
                        request.name(),
                        request.email(),
                        request.password(),
                        Role.SELLER,
                        null
                );

        User user = userService.createUser(command);

        return UserResponse.from(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request
    ) {
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        throw new UnsupportedOperationException();
    }
}
