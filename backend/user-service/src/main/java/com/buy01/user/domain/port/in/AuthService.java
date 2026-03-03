package com.buy01.user.domain.port.in;

import java.util.UUID;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.domain.model.User;

public interface AuthService {

    User register(CreateUserCommand command);

    String login(LoginCommand command);

    User getCurrentUser(UUID id);

    User updateCurrentUser(UUID id, String name, String avatar);
}
