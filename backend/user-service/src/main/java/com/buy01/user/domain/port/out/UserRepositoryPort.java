package com.buy01.user.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.buy01.user.domain.model.User;

public interface UserRepositoryPort {
    
    User save(User user);
    
    Optional<User> findById(UUID id);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsById(UUID id);
    
    void deleteById(UUID id);
}
