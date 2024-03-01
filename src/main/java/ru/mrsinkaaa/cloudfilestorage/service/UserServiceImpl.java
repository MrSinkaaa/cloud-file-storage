package ru.mrsinkaaa.cloudfilestorage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.UserNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.mapper = new ObjectMapper();
    }

    @Override
    public User createUser(UserDTO user) {
        User userEntity = mapper.convertValue(user, User.class);
        return userRepository.save(userEntity);
    }

    @Override
    public UserDTO getUserByLogin(String login) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + login));

        return mapper.convertValue(user, UserDTO.class);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> mapper.convertValue(user, UserDTO.class))
                .toList();
    }

    @Override
    public User updateUser(String login, User userDetails) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + login));

        user.setPassword(userDetails.getPassword());

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String login) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + login));

        userRepository.delete(user);
    }
}
