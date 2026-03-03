package com.buy01.user.infrastructure.adapters.out;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.out.UserRepositoryPort;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final Map<UUID, User> userStore = new ConcurrentHashMap<>();
    private final Map<String, User> emailIndex = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        userStore.put(user.getId(), user);
        emailIndex.put(user.getEmail(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(userStore.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(emailIndex.get(email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }

    @Override
    public void deleteById(UUID id) {
        User user = userStore.remove(id);
        if (user != null) {
            emailIndex.remove(user.getEmail());
        }
    }
}
