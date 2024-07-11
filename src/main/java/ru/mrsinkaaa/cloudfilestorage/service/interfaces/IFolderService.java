package ru.mrsinkaaa.cloudfilestorage.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;


import java.util.List;

public interface IFolderService {

    Folder findByOwnerAndFolderName(User owner, String folderName);
    Folder findByFolderName(String folderName);
    Folder findByFolderId(Long id);
    FolderDTO findByFolderName(User owner, String folderName);
    List<FolderDTO> findSubFolders(Long id);
    void deleteFolder(User owner, Folder folder);
    Folder createFolder(User owner, String folderName, String parentFolderName);
}
