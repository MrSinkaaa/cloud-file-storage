package ru.mrsinkaaa.cloudfilestorage.service.interfaces;

import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.List;

public interface IFolderService {

    Folder findByFolderId(Long id);
    Folder findByFolderName(String folderName);
    Folder findByOwnerAndFolderName(User owner, String folderName);
    Folder findByOwnerAndFolderId(User owner, Long folderId);
    FolderDTO findByOwnerAndMinioObjectId(User owner, String minioObjectId);
    FolderDTO findByFolderName(User owner, String folderName);
    List<FolderDTO> findSubFolders(Long id);
    List<FolderDTO> findFoldersByOwnerAndFolderNameContainingIgnoreCase(User owner, String folderName);
    Folder renameFolder(User owner, Long folderId, String newFolderName);
    Folder replaceFolder(User owner, Long id, Folder newParentFolder);
    void deleteFolder(User owner, Long folderId);
    Folder createFolder(User owner, String folderName, String parentFolderName);
}
