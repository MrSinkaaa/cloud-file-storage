package ru.mrsinkaaa.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.UserAlreadyExistsException;
import ru.mrsinkaaa.cloudfilestorage.repository.UserRepository;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IFolderService;
import ru.mrsinkaaa.cloudfilestorage.service.interfaces.IUserService;

import java.util.Collections;

import static ru.mrsinkaaa.cloudfilestorage.util.MinioRootFolderUtils.getUserRootFolderPrefix;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService, IUserService {

    private final UserRepository userRepository;
    private final IFolderService folderService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).map(user ->
                new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Collections.emptyList()))
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
    }

    public void registerUser(UserDTO userDTO) {
        if(userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(userDTO.getUsername() + " already exists");
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .build();

        userRepository.save(user);
        folderService.createFolder(user, getUserRootFolderPrefix(userDTO.getUsername()), null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Failed to retrieve user: " + username));
    }
}
