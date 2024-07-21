package ru.mrsinkaaa.cloudfilestorage.exception;

import org.springframework.http.HttpStatus;

public class FileNotFoundException extends ApplicationException {

    public FileNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
