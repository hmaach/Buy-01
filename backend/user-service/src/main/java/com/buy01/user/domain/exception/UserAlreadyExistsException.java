package com.buy01.user.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
