package ru.mrsinkaaa.cloudfilestorage.service;

import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.List;

public interface UserService {

    User createUser(UserDTO user);
    UserDTO getUserByLogin(String login);
    List<UserDTO> getAllUsers();
    User updateUser(String login, User user);
    void deleteUser(String login);
}
