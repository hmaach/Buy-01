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
    private String avatar;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserEntity create(String name, String email, String password, Role role) {
        Instant now = Instant.now();
        return new UserEntity(
                UUID.randomUUID(),
                name,
                email,
                password,
                role,
                null,
                now,
                now
        );
    }

    public void update(String name, String avatar) {
        this.name = name;
        if (avatar != null) {
            this.avatar = avatar;
        }
        this.updatedAt = Instant.now();
    }
}
