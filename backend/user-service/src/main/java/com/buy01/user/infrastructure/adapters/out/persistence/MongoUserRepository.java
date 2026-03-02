package com.buy01.user.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.buy01.user.infrastructure.adapters.out.persistence.entity.UserEntity;

public interface MongoUserRepository extends MongoRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
