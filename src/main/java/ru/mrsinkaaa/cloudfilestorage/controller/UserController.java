package ru.mrsinkaaa.cloudfilestorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String register() {
        return "authorization";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "authorization";
    }


}
