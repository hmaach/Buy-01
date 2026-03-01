package com.buy01.user.domain.model;

import java.time.Instant;
import java.util.UUID;

public class User {
    
    private UUID id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private String avatar;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {}

    public User(UUID id, String name, String email, String password, Role role, String avatar, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.avatar = avatar;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static User create(String name, String email, String password, Role role) {
        Instant now = Instant.now();
        return new User(
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
