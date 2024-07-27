package ru.mrsinkaaa.cloudfilestorage.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStorageException extends ApplicationException {

    public InsufficientStorageException(String message) {
        super(message, HttpStatus.INSUFFICIENT_STORAGE.value());
    }
}
