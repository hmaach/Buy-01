package com.buy01.user.domain.port.in;

import com.buy01.user.application.command.RegisterCommand;
import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.domain.model.User;

public interface AuthService {
    
    User register(RegisterCommand command);
    
    String login(LoginCommand command);
    
    User getCurrentUser(String email);
    
    User updateCurrentUser(String email, String name, String avatar);
}
