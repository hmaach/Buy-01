package com.buy01.user.domain.model.ValueObjects;

public class Password {
    
    private final String value;

    public Password(String value) {
        if (value == null || value.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return value.equals(password.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
