package ru.mrsinkaaa.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.mrsinkaaa.cloudfilestorage.dto.UserDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.UserAlreadyExistsException;
import ru.mrsinkaaa.cloudfilestorage.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FolderService folderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_success() {
        UserDTO userDTO = UserDTO.builder()
                .username("testuser")
                .password("passwrod")
                .build();

        User user = User.builder()
                .username(userDTO.getUsername())
                .password("encodedPassword")
                .build();

        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.registerUser(userDTO);

        verify(userRepository).save(any(User.class));
        verify(folderService).createFolder(any(User.class), eq("user-testuser-files/"), eq(null));
    }

    @Test
    void registerUser_userAlreadyExists() {
        UserDTO userDTO = UserDTO.builder()
                .username("testuser")
                .password("passwrod")
                .build();

        User user = User.builder()
                .username(userDTO.getUsername())
                .build();

        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));

        UserAlreadyExistsException exception =
                assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(userDTO));
        assertEquals("testuser already exists", exception.getMessage());
    }
}
