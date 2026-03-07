package com.buy01.user.domain.port.in;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.out.TokenResult;

public interface AuthService {

    User register(CreateUserCommand command);

    TokenResult login(LoginCommand command);

}
