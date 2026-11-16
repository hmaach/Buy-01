package com.buy01.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buy01.user.application.command.CreateUserCommand;
import com.buy01.user.application.command.LoginCommand;
import com.buy01.user.domain.exception.UserAlreadyExistsException;
import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.out.PasswordEncoderPort;
import com.buy01.user.domain.port.out.TokenGeneratorPort;
import com.buy01.user.domain.port.out.TokenResult;
import com.buy01.user.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenGeneratorPort tokenGenerator;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_fails_when_email_already_used() {
        CreateUserCommand command = new CreateUserCommand("Hamza", "taken@buy01.test", "secret123", Role.CLIENT, null);
        when(userRepository.existsByEmail("taken@buy01.test")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(command));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_stores_encoded_password_not_raw() {
        CreateUserCommand command = new CreateUserCommand("Hamza", "new@buy01.test", "secret123", Role.SELLER, null);
        when(userRepository.existsByEmail("new@buy01.test")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");

        authService.register(command);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("encoded-secret", savedUser.getValue().getPassword());
    }

    @Test
    void login_returns_token_for_valid_credentials() {
        User user = User.create("Hamza", "hamza@buy01.test", "encoded-secret", Role.SELLER);
        TokenResult token = new TokenResult("jwt-token", Instant.parse("2026-01-01T00:00:00Z"));
        when(userRepository.findByEmail("hamza@buy01.test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded-secret")).thenReturn(true);
        when(tokenGenerator.generateToken(user.getId(), "hamza@buy01.test", Role.SELLER)).thenReturn(token);

        TokenResult result = authService.login(new LoginCommand("hamza@buy01.test", "secret123"));

        assertEquals(token, result);
    }
}
