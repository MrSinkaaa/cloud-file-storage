package ru.mrsinkaaa.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
}
