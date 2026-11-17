package com.buy01.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.buy01.user.application.command.UpdateUserCommand;
import com.buy01.user.application.events.UserDeletedEvent;
import com.buy01.user.domain.exception.UserNotFoundException;
import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AvatarService;
import com.buy01.user.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private AvatarService avatarService;

    @Mock
    private KafkaTemplate<String, UserDeletedEvent> userDeleteTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUser_replaces_avatar_and_deletes_old_file() {
        User user = User.create("Hamza", "hamza@buy01.test", "encoded-secret", Role.SELLER);
        user.setAvatarUrl("/avatars/old.png");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.updateUser(user.getId(), new UpdateUserCommand(null, null, "/avatars/new.png"));

        verify(avatarService).deleteAvatar("/avatars/old.png");
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("/avatars/new.png", savedUser.getValue().getAvatarUrl());
    }

    @Test
    void deleteUser_removes_user_and_publishes_user_deleted_event() {
        User user = User.create("Hamza", "hamza@buy01.test", "encoded-secret", Role.SELLER);
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
        ArgumentCaptor<UserDeletedEvent> event = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(userDeleteTemplate).send(eq("user-deleted"), eq(userId.toString()), event.capture());
        assertEquals(userId, event.getValue().userId());
    }

    @Test
    void deleteUser_fails_and_publishes_nothing_when_user_missing() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(unknownId));

        verify(userRepository, never()).deleteById(any());
        verify(userDeleteTemplate, never()).send(anyString(), anyString(), any());
    }
}
