package ru.mrsinkaaa.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

}
