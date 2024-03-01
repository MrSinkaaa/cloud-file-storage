package ru.mrsinkaaa.cloudfilestorage.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.mrsinkaaa.cloudfilestorage.exception.UserAlreadyExistsException;

@ControllerAdvice
public class UserAlreadyExistsAdvice {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseBody
    String userAlreadyExistsHandler(UserAlreadyExistsException ex) {
        return ex.getMessage();
    }
}
