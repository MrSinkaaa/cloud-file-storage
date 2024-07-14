package ru.mrsinkaaa.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByParentFolder(Folder parentFolder);
    Optional<Folder> findByFolderName(String folderName);
    Optional<Folder> findByOwnerAndFolderName(User owner, String folderName);
    Optional<Folder> findByIdAndOwner(Long id, User owner);


}
