package ru.mrsinkaaa.cloudfilestorage.service.interfaces;


import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;


import java.util.List;

public interface IFileService {

    FileDTO findByFileName(String fileName);
    File findFileByOwnerIdAndId(Long ownerId, Long id);
    List<FileDTO> findByFolderId(Folder folderId);
    File renameFile(String oldFileName, String newFileName);
    File deleteFile(User user, Long id);
}
