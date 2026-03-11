package com.buy01.user.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.out.UserRepositoryPort;
import com.buy01.user.infrastructure.adapters.out.persistence.entity.UserEntity;
import com.buy01.user.infrastructure.adapters.out.persistence.mapper.UserEntityMapper;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final MongoUserRepository mongoRepository;

    public UserRepositoryAdapter(MongoUserRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntityMapper.toEntity(user);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        UserEntity saved = mongoRepository.save(entity);
        return UserEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return mongoRepository.findById(id)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoRepository.findByEmail(email)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return mongoRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        mongoRepository.deleteById(id);
    }
}
