package ru.mrsinkaaa.cloudfilestorage.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApplicationException {

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
