package com.buy01.user.infrastructure.adapters.out.persistence.mapper;

import com.buy01.user.domain.model.User;
import com.buy01.user.infrastructure.adapters.out.persistence.entity.UserEntity;

public class UserEntityMapper {

    public static UserEntity toEntity(User user) {
        UserEntity e = new UserEntity();
        e.setId(user.getId());
        e.setName(user.getName());
        e.setEmail(user.getEmail());
        e.setPassword(user.getPassword());
        e.setRole(user.getRole());
        e.setAvatarId(user.getAvatarId());
        e.setCreatedAt(user.getCreatedAt());
        e.setUpdatedAt(user.getUpdatedAt());
        return e;
    }

    public static User toDomain(UserEntity e) {
        User user = new User();
        user.setId(e.getId());
        user.setName(e.getName());
        user.setEmail(e.getEmail());
        user.setPassword(e.getPassword());
        user.setRole(e.getRole());
        user.setAvatarId(e.getAvatarId());
        user.setCreatedAt(e.getCreatedAt());
        user.setUpdatedAt(e.getUpdatedAt());
        return user;
    }
}
