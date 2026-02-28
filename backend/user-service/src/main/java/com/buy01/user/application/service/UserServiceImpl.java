package com.buy01.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.UpdateUserCommand;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.UserService;

@Service
public class UserServiceImpl implements UserService {

    // private final UserRepositoryPort userRepository;
    // private final PasswordEncoderPort passwordEncoder;
    // private final TokenGeneratorPort tokenGenerator;

    // public UserServiceImpl(
    //         UserRepositoryPort userRepository,
    //         PasswordEncoderPort passwordEncoder,
    //         TokenGeneratorPort tokenGenerator
    // ) {
    //     this.userRepository = userRepository;
    //     this.passwordEncoder = passwordEncoder;
    //     this.tokenGenerator = tokenGenerator;
    // }

    @Override
    public User findById(UUID userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User createUser(CreateUserCommand command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User updateUser(UUID userId, UpdateUserCommand command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(UUID userId) {
        throw new UnsupportedOperationException();
    }
}
