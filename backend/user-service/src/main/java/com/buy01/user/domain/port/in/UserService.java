package com.buy01.user.domain.port.in;

import java.util.UUID;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.UpdateUserCommand;
import com.buy01.user.domain.model.User;

public interface UserService {

    User findById(UUID userId);

    User createUser(CreateUserCommand command);

    User updateUser(UUID userId, UpdateUserCommand command);

    void deleteUser(UUID userId);

    User findByEmail(String email);
}
