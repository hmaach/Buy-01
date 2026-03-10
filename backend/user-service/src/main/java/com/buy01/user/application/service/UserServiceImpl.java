package com.buy01.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buy01.user.application.command.UpdateUserCommand;
import com.buy01.user.domain.exception.UserNotFoundException;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AvatarService;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.domain.port.out.UserRepositoryPort;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepository;
    private final AvatarService avatarService;

    public UserServiceImpl(UserRepositoryPort userRepository, AvatarService avatarService) {
        this.userRepository = userRepository;
        this.avatarService = avatarService;
    }

    @Override
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    @Override
    public User updateUser(UUID userId, UpdateUserCommand command) {
        User user = findById(userId);
        
        // Handle avatar update - delete old avatar if new one is provided
        String newAvatarUrl = command.avatarUrl();
        if (newAvatarUrl != null && !newAvatarUrl.isEmpty()) {
            // Delete old avatar if exists
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                avatarService.deleteAvatar(user.getAvatarUrl());
            }
            user.setAvatarUrl(newAvatarUrl);
        }
        
        user.update(command.name(), command.email(), newAvatarUrl);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // Delete avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            avatarService.deleteAvatar(user.getAvatarUrl());
        }
        
        userRepository.deleteById(userId);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }
}
