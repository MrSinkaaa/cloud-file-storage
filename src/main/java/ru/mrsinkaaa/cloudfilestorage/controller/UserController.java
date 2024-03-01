package ru.mrsinkaaa.cloudfilestorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.service.UserService;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{login}")
    public UserDTO getUserByLogin(@PathVariable String login) {
        return userService.getUserByLogin(login);
    }

    @PostMapping("/users")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        userService.createUser(userDTO);
        return userDTO;
    }
}
