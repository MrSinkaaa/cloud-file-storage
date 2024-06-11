package ru.mrsinkaaa.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByFileName(String fileName);
    Optional<File> findByOwnerIdAndId(Long ownerId, Long id);
    List<File> findByFolderId(Folder folderId);
}
