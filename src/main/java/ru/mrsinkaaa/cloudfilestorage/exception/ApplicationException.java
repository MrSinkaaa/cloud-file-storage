package ru.mrsinkaaa.cloudfilestorage.exception;

public abstract class ApplicationException extends RuntimeException {

    private final int statusCode;

    public ApplicationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
