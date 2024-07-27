package ru.mrsinkaaa.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByOwnerAndFileName(User owner, String fileName);
    Optional<File> findByOwnerIdAndId(Long ownerId, Long id);
    List<File> findByFolderId(Folder folderId);
    List<File> findFilesByOwnerAndFileNameContainingIgnoreCase(User owner, String fileName);

    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.owner.id = :ownerId")
    Long getTotalUsedRamByUser(Long ownerId);
}
