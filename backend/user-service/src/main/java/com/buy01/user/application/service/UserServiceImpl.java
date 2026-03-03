package com.buy01.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.UpdateUserCommand;
import com.buy01.user.domain.exception.UserNotFoundException;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.domain.port.out.UserRepositoryPort;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepository;

    public UserServiceImpl(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    @Override
    public User createUser(CreateUserCommand command) {
        User user = User.create(
                command.name(),
                command.email(),
                command.password(),
                command.role()
        );
        return userRepository.save(user);
    }

    @Override
    public User updateUser(UUID userId, UpdateUserCommand command) {
        User user = findById(userId);
        user.update(command.name(), command.avatar());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        userRepository.deleteById(userId);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}
