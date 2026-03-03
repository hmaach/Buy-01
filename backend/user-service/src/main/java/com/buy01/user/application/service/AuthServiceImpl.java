package com.buy01.user.application.service;

import org.springframework.stereotype.Service;

import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.application.command.RegisterCommand;
import com.buy01.user.domain.exception.InvalidCredentialsException;
import com.buy01.user.domain.exception.UserAlreadyExistsException;
import com.buy01.user.domain.exception.UserNotFoundException;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AuthService;
import com.buy01.user.domain.port.out.PasswordEncoderPort;
import com.buy01.user.domain.port.out.TokenGeneratorPort;
import com.buy01.user.domain.port.out.UserRepositoryPort;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public AuthServiceImpl(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenGeneratorPort tokenGenerator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public User register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("User with email " + command.email() + " already exists");
        }

        String encodedPassword = passwordEncoder.encode(command.password());
        
        User user = User.create(
                command.name(),
                command.email(),
                encodedPassword,
                command.role()
        );

        return userRepository.save(user);
    }

    @Override
    public String login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return tokenGenerator.generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Override
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public User updateCurrentUser(String email, String name, String avatar) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.update(name, avatar);
        
        return userRepository.save(user);
    }
}
