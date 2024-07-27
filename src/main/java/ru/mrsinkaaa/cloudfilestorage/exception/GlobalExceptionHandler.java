package ru.mrsinkaaa.cloudfilestorage.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import ru.mrsinkaaa.cloudfilestorage.dto.ErrorDTO;
import ru.mrsinkaaa.cloudfilestorage.service.StorageViewService;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final StorageViewService storageViewService;
    private final UserService userService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGlobalException(HttpServletRequest req, Exception ex,
                                              @AuthenticationPrincipal User user) {

        log.error("Error message: {}", ex.getMessage());

        var owner = userService.findByUsername(user.getUsername());
        ErrorDTO errorDTO = new ErrorDTO(ex.getMessage());
        String path = req.getParameter("path") != null ? req.getParameter("path") : "";

        return storageViewService.getMainPageWithError(owner, path, errorDTO);
    }

}
