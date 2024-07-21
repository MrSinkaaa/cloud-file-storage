package ru.mrsinkaaa.cloudfilestorage.exception;

import org.springframework.http.HttpStatus;

public class FileDownloadException extends ApplicationException {

    public FileDownloadException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
