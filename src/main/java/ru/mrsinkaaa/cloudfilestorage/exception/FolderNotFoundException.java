package ru.mrsinkaaa.cloudfilestorage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class FolderNotFoundException extends ApplicationException {

    public FolderNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
