package ru.mrsinkaaa.cloudfilestorage.service.interfaces;


import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.File;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;


import java.io.InputStream;
import java.util.List;

public interface IFileService {

    FileDTO findByFileName(String fileName);
    File findFileByOwnerIdAndId(Long ownerId, Long id);
    List<FileDTO> findByFolderId(Folder folderId);
    InputStream downloadFile(User owner, Long id);
    File renameFile(User owner, Long id, String newFileName);
    File deleteFile(User user, Long id);
}
