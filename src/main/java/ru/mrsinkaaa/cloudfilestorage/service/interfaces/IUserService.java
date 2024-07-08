package ru.mrsinkaaa.cloudfilestorage.service.interfaces;

import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

public interface IUserService {

    void registerUser(UserDTO userDTO);
    User findByUsername(String username);
}
