package com.buy01.user.infrastructure.adapters.out.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.buy01.user.domain.model.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private UUID id;

    private String name;
    private String email;
    private String password;
    private Role role;
    private UUID avatarId;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserEntity create(String name, String email, String password, Role role, UUID avatarId) {
        Instant now = Instant.now();
        return new UserEntity(
                UUID.randomUUID(),
                name,
                email,
                password,
                role,
                avatarId,
                now,
                now
        );
    }

    public void update(String name, UUID avatarId) {
        this.name = name;
        if (avatarId != null) {
            this.avatarId = avatarId;
        }
        this.updatedAt = Instant.now();
    }
}
